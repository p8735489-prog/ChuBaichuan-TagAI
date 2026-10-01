package com.kuzulabz.waifutaggercn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat

/**
 * 推理前台服务：识别期间显示常驻通知并获取 WakeLock 防止 CPU 休眠，
 * 切到后台后推理不会被系统杀死。
 * 通过 ACTION_STOP_BROADCAST 广播通知 Activity 取消推理任务。
 */
class InferenceForegroundService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_STOP_BROADCAST) {
                releaseWakeLock()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val filter = IntentFilter(ACTION_STOP_BROADCAST)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stopReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(stopReceiver, filter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { unregisterReceiver(stopReceiver) }
        releaseWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val initialText = getString(R.string.workflow_progress_prepare)
                val notification = buildNotification(0, initialText)
                startForeground(NOTIFICATION_ID, notification)
                acquireWakeLock()
            }
            ACTION_UPDATE -> {
                val percent = intent.getIntExtra(EXTRA_PERCENT, 0)
                val stage = intent.getStringExtra(EXTRA_STAGE) ?: ""
                updateNotification(percent, stage)
            }
            ACTION_STOP -> {
                releaseWakeLock()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.inference_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.inference_notification_channel)
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(percent: Int, stage: String): android.app.Notification {
        val openIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 停止按钮：发送广播通知 Activity 取消推理
        val stopIntent = Intent(ACTION_STOP_BROADCAST).setPackage(packageName)
        val stopPendingIntent = PendingIntent.getBroadcast(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = getString(R.string.inference_notification_text, percent, stage)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.inference_notification_title))
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_crop)
            .setContentIntent(pendingIntent)
            .setProgress(100, percent.coerceIn(0, 100), false)
            .setOngoing(true)
            .setSilent(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.inference_stop),
                stopPendingIntent
            )
            .build()
    }

    private fun updateNotification(percent: Int, stage: String) {
        val notification = buildNotification(percent, stage)
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "WaifuTaggerCN:InferenceWakeLock"
            ).apply {
                setReferenceCounted(false)
                acquire(10 * 60 * 1000L) // 最长10分钟，防止泄漏
            }
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) runCatching { it.release() }
        }
        wakeLock = null
    }

    companion object {
        private const val CHANNEL_ID = "inference_progress"
        private const val NOTIFICATION_ID = 2001

        const val ACTION_START = "inference_start"
        const val ACTION_UPDATE = "inference_update"
        const val ACTION_STOP = "inference_stop"
        const val ACTION_STOP_BROADCAST = "com.kuzulabz.waifutaggercn.INFERENCE_STOP"

        const val EXTRA_PERCENT = "percent"
        const val EXTRA_STAGE = "stage"

        fun start(context: Context) {
            val intent = Intent(context, InferenceForegroundService::class.java)
                .setAction(ACTION_START)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun update(context: Context, percent: Int, stage: String) {
            val intent = Intent(context, InferenceForegroundService::class.java)
                .setAction(ACTION_UPDATE)
                .putExtra(EXTRA_PERCENT, percent)
                .putExtra(EXTRA_STAGE, stage)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, InferenceForegroundService::class.java)
                .setAction(ACTION_STOP)
            context.startService(intent)
        }
    }
}
