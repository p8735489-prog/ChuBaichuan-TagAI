package com.kuzulabz.waifutaggercn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AiModelDownloadService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile private var cancelled = false

    // WakeLock：下载期间保持 CPU 唤醒，防止息屏后系统休眠中断下载
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CANCEL -> {
                cancelled = true
                // 立即断开所有活跃的 HTTP 连接，使阻塞中的 input.read() 抛出异常，
                // 从而让下载线程立即响应取消，而不是等待 readTimeout（30 秒）
                DownloadConnectionTracker.disconnectAll()
                val progress = currentProgress?.copy(phase = getString(R.string.ai_model_download_cancelling))
                currentProgress = progress
                progress?.let { updateNotification(it, currentModelId ?: "") }
                sendState(ACTION_PROGRESS, progress, getString(R.string.ai_model_download_cancelling))
                if (!isRunning) {
                    sendState(ACTION_CANCELLED, progress, getString(R.string.ai_model_download_cancelled))
                    stopForeground(STOP_FOREGROUND_DETACH)
                    stopSelf()
                }
                // 取消时不需要重启
                return START_NOT_STICKY
            }
            ACTION_START -> {
                // 持久化下载 Intent，以便进程被杀后恢复
                intent?.let { persistDownloadIntent(it) }
                startDownload(intent)
            }
            ACTION_RESTART -> {
                // onTaskRemoved 触发的重启：从持久化存储恢复下载
                if (!isRunning) {
                    val savedIntent = loadPersistedIntent()
                    if (savedIntent != null) {
                        Log.i(TAG, "Service restarted via onTaskRemoved, resuming download")
                        startDownload(savedIntent)
                    }
                }
            }
            null -> {
                // 服务被系统杀死后重启（START_STICKY），尝试从持久化存储恢复下载
                if (!isRunning) {
                    val savedIntent = loadPersistedIntent()
                    if (savedIntent != null) {
                        Log.i(TAG, "Service restarted by system, resuming download from persisted state")
                        startDownload(savedIntent)
                    } else {
                        Log.i(TAG, "Service restarted by system, no pending download found")
                        stopSelf()
                        return START_NOT_STICKY
                    }
                }
            }
        }
        // 下载进行中返回 START_STICKY，系统杀死后会尝试重启服务
        return if (isRunning) START_STICKY else START_NOT_STICKY
    }

    /**
     * 当用户从最近任务列表划掉应用时，重启服务以保持下载继续。
     * 这是保活的关键一环：前台服务在被划掉后可能被系统杀死，
     * 通过在这里重新启动自身，可以最大程度保持下载不中断。
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        if (isRunning) {
            Log.i(TAG, "Task removed, restarting service to keep download alive")
            val restartIntent = Intent(applicationContext, AiModelDownloadService::class.java)
                .setAction(ACTION_RESTART)
            val pendingIntent = PendingIntent.getService(
                applicationContext,
                1,
                restartIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.set(
                android.app.AlarmManager.ELAPSED_REALTIME,
                android.os.SystemClock.elapsedRealtime() + 1000,
                pendingIntent
            )
        }
        super.onTaskRemoved(rootIntent)
    }

    private fun startDownload(intent: Intent) {
        if (isRunning) return
        val modelId = intent.getStringExtra(EXTRA_MODEL_ID) ?: return
        val displayName = intent.getStringExtra(EXTRA_DISPLAY_NAME) ?: modelId
        val repoName = intent.getStringExtra(EXTRA_REPO_NAME) ?: return
        val sizeLabel = intent.getStringExtra(EXTRA_SIZE_LABEL) ?: ""
        val family = intent.getStringExtra(EXTRA_FAMILY) ?: ""
        val strengthRank = intent.getIntExtra(EXTRA_STRENGTH_RANK, 0)
        val speedRank = intent.getIntExtra(EXTRA_SPEED_RANK, 0)
        val source = intent.getStringExtra(EXTRA_SOURCE) ?: "huggingface"
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: "tagger"
        val hfRepo = intent.getStringExtra(EXTRA_HF_REPO) ?: ""
        val onnxFile = intent.getStringExtra(EXTRA_ONNX_FILE) ?: ""
        val tagFile = intent.getStringExtra(EXTRA_TAG_FILE) ?: ""
        val model = DownloadableAiModel(
            id = modelId,
            displayName = displayName,
            descriptionResId = R.string.ai_model_switch_summary,
            repoName = repoName,
            sizeLabel = sizeLabel,
            family = family,
            strengthRank = strengthRank,
            speedRank = speedRank,
            category = category,
            hfRepo = hfRepo,
            onnxFile = onnxFile,
            tagFile = tagFile
        )

        // 保存 intent 以便服务被杀死后恢复
        pendingDownloadIntent = intent

        cancelled = false
        isRunning = true
        currentModelId = modelId
        currentProgress = DownloadProgress(modelId, getString(R.string.ai_model_preparing_download), 0, 0L, -1L)
        createNotificationChannel()
        acquireWakeLock()
        startForegroundCompat(currentProgress!!, displayName)
        sendState(ACTION_PROGRESS, currentProgress, null)

        serviceScope.launch {
            var retryCount = 0
            val maxRetries = 2

            while (retryCount <= maxRetries) {
                if (cancelled || !isActive) break

                val result = downloadAiModelBundle(
                    context = this@AiModelDownloadService,
                    model = model,
                    source = source,
                    onProgress = { progress ->
                        currentProgress = progress
                        updateNotification(progress, displayName)
                        sendState(ACTION_PROGRESS, progress, null)
                    },
                    isCancelled = { cancelled || !isActive }
                )

                if (result.success || cancelled) {
                    finishDownload(result, displayName, model)
                    return@launch
                }

                // 下载失败，尝试重试
                retryCount++
                if (retryCount <= maxRetries && !cancelled) {
                    Log.w(TAG, "Download failed (attempt $retryCount/$maxRetries): ${result.message}, retrying...")
                    currentProgress = DownloadProgress(
                        modelId,
                        getString(R.string.retrying_download, retryCount, maxRetries),
                        currentProgress?.percent ?: 0,
                        currentProgress?.receivedBytes ?: 0L,
                        currentProgress?.totalBytes ?: -1L
                    )
                    currentProgress?.let {
                        updateNotification(it, displayName)
                        sendState(ACTION_PROGRESS, it, null)
                    }
                    kotlinx.coroutines.delay(2000L * retryCount)
                } else {
                    finishDownload(result, displayName, model)
                    return@launch
                }
            }
        }
    }

    private fun finishDownload(
        result: AiModelDownloadResult,
        displayName: String,
        model: DownloadableAiModel
    ) {
        isRunning = false
        pendingDownloadIntent = null
        clearPersistedIntent()
        lastResultSuccess = result.success
        lastResultMessage = result.message
        lastResultModelId = result.modelId
        sendState(if (result.success) ACTION_FINISHED else ACTION_FAILED, currentProgress, result.message, result.modelId)
        updateNotification(
            currentProgress?.copy(
                percent = if (result.success) 100 else (currentProgress?.percent ?: 0),
                phase = result.message,
                isVerifying = false
            ) ?: DownloadProgress(model.id, result.message, if (result.success) 100 else 0, 0L, -1L),
            displayName,
            finished = true,
            success = result.success
        )
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_DETACH)
        getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
        stopSelf()
    }

    /**
     * 获取 WakeLock，不设置超时时间。
     * 大模型下载可能需要很长时间（数十分钟），10分钟超时会导致下载中断。
     * WakeLock 会在 finishDownload/onDestroy 中释放，不会泄漏。
     */
    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$TAG:download").apply {
            setReferenceCounted(false)
            acquire() // 不设置超时，由 finishDownload/onDestroy 负责释放
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    private fun startForegroundCompat(progress: DownloadProgress, modelName: String) {
        val notification = buildNotification(progress, modelName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(
        progress: DownloadProgress,
        modelName: String,
        finished: Boolean = false,
        success: Boolean = false
    ) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(progress, modelName, finished, success))
    }

    private fun buildNotification(
        progress: DownloadProgress,
        modelName: String,
        finished: Boolean = false,
        success: Boolean = false
    ) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setContentTitle(
            when {
                finished && success -> getString(R.string.ai_model_download_complete_title)
                finished -> getString(R.string.ai_model_download_failed_title)
                progress.isVerifying -> getString(R.string.ai_model_verifying_title)
                else -> getString(R.string.ai_model_downloading)
            }
        )
        .setContentText("$modelName · ${progress.percent}% · ${progress.phase}")
        .setOngoing(!finished)
        .setOnlyAlertOnce(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        .setProgress(100, progress.percent.coerceIn(0, 100), progress.totalBytes <= 0L && progress.percent == 0)
        .addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            getString(R.string.ai_model_download_abandon),
            PendingIntent.getService(
                this,
                1,
                Intent(this, AiModelDownloadService::class.java).setAction(ACTION_CANCEL),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.ai_model_download_channel),
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun sendState(action: String, progress: DownloadProgress?, message: String?, modelPath: String? = null) {
        val intent = Intent(action).setPackage(packageName)
        progress?.let {
            intent.putExtra(EXTRA_MODEL_ID, it.modelId)
            intent.putExtra(EXTRA_PHASE, it.phase)
            intent.putExtra(EXTRA_PERCENT, it.percent)
            intent.putExtra(EXTRA_RECEIVED_BYTES, it.receivedBytes)
            intent.putExtra(EXTRA_TOTAL_BYTES, it.totalBytes)
            intent.putExtra(EXTRA_VERIFYING, it.isVerifying)
        }
        intent.putExtra(EXTRA_MESSAGE, message)
        intent.putExtra(EXTRA_MODEL_PATH, modelPath)
        sendBroadcast(intent)
    }

    /**
     * 将下载 Intent 持久化到 SharedPreferences。
     * 当进程被系统杀死后，START_STICKY 重启服务时可以从这里恢复下载参数。
     */
    private fun persistDownloadIntent(intent: Intent) {
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString(KEY_PENDING_MODEL_ID, intent.getStringExtra(EXTRA_MODEL_ID))
                putString(KEY_PENDING_DISPLAY_NAME, intent.getStringExtra(EXTRA_DISPLAY_NAME))
                putString(KEY_PENDING_REPO_NAME, intent.getStringExtra(EXTRA_REPO_NAME))
                putString(KEY_PENDING_SIZE_LABEL, intent.getStringExtra(EXTRA_SIZE_LABEL))
                putString(KEY_PENDING_FAMILY, intent.getStringExtra(EXTRA_FAMILY))
                putInt(KEY_PENDING_STRENGTH_RANK, intent.getIntExtra(EXTRA_STRENGTH_RANK, 0))
                putInt(KEY_PENDING_SPEED_RANK, intent.getIntExtra(EXTRA_SPEED_RANK, 0))
                putString(KEY_PENDING_SOURCE, intent.getStringExtra(EXTRA_SOURCE))
                putString(KEY_PENDING_CATEGORY, intent.getStringExtra(EXTRA_CATEGORY))
                putString(KEY_PENDING_HF_REPO, intent.getStringExtra(EXTRA_HF_REPO))
                putString(KEY_PENDING_ONNX_FILE, intent.getStringExtra(EXTRA_ONNX_FILE))
                putString(KEY_PENDING_TAG_FILE, intent.getStringExtra(EXTRA_TAG_FILE))
                putBoolean(KEY_HAS_PENDING, true)
            }.apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to persist download intent: ${e.message}")
        }
    }

    private fun loadPersistedIntent(): Intent? {
        return try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            if (!prefs.getBoolean(KEY_HAS_PENDING, false)) return null
            Intent(this, AiModelDownloadService::class.java).setAction(ACTION_START).apply {
                putExtra(EXTRA_MODEL_ID, prefs.getString(KEY_PENDING_MODEL_ID, null))
                putExtra(EXTRA_DISPLAY_NAME, prefs.getString(KEY_PENDING_DISPLAY_NAME, null))
                putExtra(EXTRA_REPO_NAME, prefs.getString(KEY_PENDING_REPO_NAME, null))
                putExtra(EXTRA_SIZE_LABEL, prefs.getString(KEY_PENDING_SIZE_LABEL, ""))
                putExtra(EXTRA_FAMILY, prefs.getString(KEY_PENDING_FAMILY, ""))
                putExtra(EXTRA_STRENGTH_RANK, prefs.getInt(KEY_PENDING_STRENGTH_RANK, 0))
                putExtra(EXTRA_SPEED_RANK, prefs.getInt(KEY_PENDING_SPEED_RANK, 0))
                putExtra(EXTRA_SOURCE, prefs.getString(KEY_PENDING_SOURCE, "huggingface"))
                putExtra(EXTRA_CATEGORY, prefs.getString(KEY_PENDING_CATEGORY, "tagger"))
                putExtra(EXTRA_HF_REPO, prefs.getString(KEY_PENDING_HF_REPO, ""))
                putExtra(EXTRA_ONNX_FILE, prefs.getString(KEY_PENDING_ONNX_FILE, ""))
                putExtra(EXTRA_TAG_FILE, prefs.getString(KEY_PENDING_TAG_FILE, ""))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load persisted intent: ${e.message}")
            null
        }
    }

    private fun clearPersistedIntent() {
        try {
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().clear().apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear persisted intent: ${e.message}")
        }
    }

    override fun onDestroy() {
        DownloadConnectionTracker.disconnectAll()
        releaseWakeLock()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "AiModelDownloadService"
        private const val PREFS_NAME = "ai_download_service_state"
        private const val KEY_HAS_PENDING = "has_pending"
        private const val KEY_PENDING_MODEL_ID = "pending_model_id"
        private const val KEY_PENDING_DISPLAY_NAME = "pending_display_name"
        private const val KEY_PENDING_REPO_NAME = "pending_repo_name"
        private const val KEY_PENDING_SIZE_LABEL = "pending_size_label"
        private const val KEY_PENDING_FAMILY = "pending_family"
        private const val KEY_PENDING_STRENGTH_RANK = "pending_strength_rank"
        private const val KEY_PENDING_SPEED_RANK = "pending_speed_rank"
        private const val KEY_PENDING_SOURCE = "pending_source"
        private const val KEY_PENDING_CATEGORY = "pending_category"
        private const val KEY_PENDING_HF_REPO = "pending_hf_repo"
        private const val KEY_PENDING_ONNX_FILE = "pending_onnx_file"
        private const val KEY_PENDING_TAG_FILE = "pending_tag_file"

        const val ACTION_START = "com.kuzulabz.waifutaggercn.action.START_MODEL_DOWNLOAD"
        const val ACTION_CANCEL = "com.kuzulabz.waifutaggercn.action.CANCEL_MODEL_DOWNLOAD"
        const val ACTION_PROGRESS = "com.kuzulabz.waifutaggercn.action.MODEL_DOWNLOAD_PROGRESS"
        const val ACTION_FINISHED = "com.kuzulabz.waifutaggercn.action.MODEL_DOWNLOAD_FINISHED"
        const val ACTION_FAILED = "com.kuzulabz.waifutaggercn.action.MODEL_DOWNLOAD_FAILED"
        const val ACTION_CANCELLED = "com.kuzulabz.waifutaggercn.action.MODEL_DOWNLOAD_CANCELLED"
        const val ACTION_RESTART = "com.kuzulabz.waifutaggercn.action.RESTART_MODEL_DOWNLOAD"

        const val EXTRA_MODEL_ID = "model_id"
        const val EXTRA_DISPLAY_NAME = "display_name"
        const val EXTRA_REPO_NAME = "repo_name"
        const val EXTRA_SIZE_LABEL = "size_label"
        const val EXTRA_FAMILY = "family"
        const val EXTRA_STRENGTH_RANK = "strength_rank"
        const val EXTRA_SPEED_RANK = "speed_rank"
        const val EXTRA_SOURCE = "source"
        const val EXTRA_CATEGORY = "category"
        const val EXTRA_HF_REPO = "hf_repo"
        const val EXTRA_ONNX_FILE = "onnx_file"
        const val EXTRA_TAG_FILE = "tag_file"
        const val EXTRA_PHASE = "phase"
        const val EXTRA_PERCENT = "percent"
        const val EXTRA_RECEIVED_BYTES = "received_bytes"
        const val EXTRA_TOTAL_BYTES = "total_bytes"
        const val EXTRA_VERIFYING = "verifying"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_MODEL_PATH = "model_path"

        private const val CHANNEL_ID = "ai_model_downloads"
        private const val NOTIFICATION_ID = 26072191

        @Volatile var isRunning: Boolean = false
        @Volatile var currentModelId: String? = null
        @Volatile var currentProgress: DownloadProgress? = null
        @Volatile var lastResultSuccess: Boolean? = null
        @Volatile var lastResultMessage: String? = null
        @Volatile var lastResultModelId: String? = null
        @Volatile var pendingDownloadIntent: Intent? = null

        fun start(context: Context, model: DownloadableAiModel, source: String) {
            val intent = Intent(context, AiModelDownloadService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_MODEL_ID, model.id)
                .putExtra(EXTRA_DISPLAY_NAME, model.displayName)
                .putExtra(EXTRA_REPO_NAME, model.repoName)
                .putExtra(EXTRA_SIZE_LABEL, model.sizeLabel)
                .putExtra(EXTRA_FAMILY, model.family)
                .putExtra(EXTRA_STRENGTH_RANK, model.strengthRank)
                .putExtra(EXTRA_SPEED_RANK, model.speedRank)
                .putExtra(EXTRA_SOURCE, source)
                .putExtra(EXTRA_CATEGORY, model.category)
                .putExtra(EXTRA_HF_REPO, model.hfRepo)
                .putExtra(EXTRA_ONNX_FILE, model.onnxFile)
                .putExtra(EXTRA_TAG_FILE, model.tagFile)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun cancel(context: Context) {
            context.startService(Intent(context, AiModelDownloadService::class.java).setAction(ACTION_CANCEL))
        }
    }
}
