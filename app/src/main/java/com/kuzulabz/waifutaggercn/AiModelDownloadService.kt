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

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CANCEL -> {
                cancelled = true
                val progress = currentProgress?.copy(phase = getString(R.string.ai_model_download_cancelling))
                currentProgress = progress
                progress?.let { updateNotification(it, currentModelId ?: "") }
                sendState(ACTION_PROGRESS, progress, getString(R.string.ai_model_download_cancelling))
                if (!isRunning) {
                    sendState(ACTION_CANCELLED, progress, getString(R.string.ai_model_download_cancelled))
                    stopForeground(STOP_FOREGROUND_DETACH)
                    stopSelf()
                }
                return START_NOT_STICKY
            }
            ACTION_START -> startDownload(intent)
        }
        return START_NOT_STICKY
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
        val model = DownloadableAiModel(
            id = modelId,
            displayName = displayName,
            descriptionResId = R.string.ai_model_switch_summary,
            repoName = repoName,
            sizeLabel = sizeLabel,
            family = family,
            strengthRank = strengthRank,
            speedRank = speedRank
        )

        cancelled = false
        isRunning = true
        currentModelId = modelId
        currentProgress = DownloadProgress(modelId, getString(R.string.ai_model_preparing_download), 0, 0L, -1L)
        createNotificationChannel()
        startForegroundCompat(currentProgress!!, displayName)
        sendState(ACTION_PROGRESS, currentProgress, null)

        serviceScope.launch {
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
            isRunning = false
            lastResultSuccess = result.success
            lastResultMessage = result.message
            lastResultModelId = result.modelId
            sendState(if (result.success) ACTION_FINISHED else ACTION_FAILED, currentProgress, result.message, result.modelId)
            updateNotification(
                currentProgress?.copy(
                    percent = if (result.success) 100 else (currentProgress?.percent ?: 0),
                    phase = result.message,
                    isVerifying = false
                ) ?: DownloadProgress(modelId, result.message, if (result.success) 100 else 0, 0L, -1L),
                displayName,
                finished = true,
                success = result.success
            )
            stopForeground(STOP_FOREGROUND_DETACH)
            getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
            stopSelf()
        }
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

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.kuzulabz.waifutaggercn.action.START_MODEL_DOWNLOAD"
        const val ACTION_CANCEL = "com.kuzulabz.waifutaggercn.action.CANCEL_MODEL_DOWNLOAD"
        const val ACTION_PROGRESS = "com.kuzulabz.waifutaggercn.action.MODEL_DOWNLOAD_PROGRESS"
        const val ACTION_FINISHED = "com.kuzulabz.waifutaggercn.action.MODEL_DOWNLOAD_FINISHED"
        const val ACTION_FAILED = "com.kuzulabz.waifutaggercn.action.MODEL_DOWNLOAD_FAILED"
        const val ACTION_CANCELLED = "com.kuzulabz.waifutaggercn.action.MODEL_DOWNLOAD_CANCELLED"

        const val EXTRA_MODEL_ID = "model_id"
        const val EXTRA_DISPLAY_NAME = "display_name"
        const val EXTRA_REPO_NAME = "repo_name"
        const val EXTRA_SIZE_LABEL = "size_label"
        const val EXTRA_FAMILY = "family"
        const val EXTRA_STRENGTH_RANK = "strength_rank"
        const val EXTRA_SPEED_RANK = "speed_rank"
        const val EXTRA_SOURCE = "source"
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
