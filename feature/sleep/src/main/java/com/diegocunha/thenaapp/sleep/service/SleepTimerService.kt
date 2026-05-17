package com.diegocunha.thenaapp.sleep.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.diegocunha.thenaapp.coreui.util.formatElapsedSeconds
import com.diegocunha.thenaapp.sleep.session.SleepSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SleepTimerService : Service() {

    private val sessionManager: SleepSessionManager by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, buildNotification("00:00"), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, buildNotification("00:00"))
        }
        observeSession()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun observeSession() {
        scope.launch {
            sessionManager.activeSession.collectLatest { session ->
                if (session == null) {
                    stopSelf()
                    return@collectLatest
                }
                while (true) {
                    val elapsed = (System.currentTimeMillis() - session.startTimeMs) / 1_000L
                    updateNotification(formatElapsedSeconds(elapsed))
                    delay(1_000L)
                }
            }
        }
    }

    private fun updateNotification(elapsed: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(elapsed))
    }

    private fun buildNotification(elapsed: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Sleep in progress")
            .setContentText(elapsed)
            .setOngoing(true)
            .setSilent(true)
            .build()

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Sleep Timer",
            NotificationManager.IMPORTANCE_LOW,
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "sleep_timer"
    }
}