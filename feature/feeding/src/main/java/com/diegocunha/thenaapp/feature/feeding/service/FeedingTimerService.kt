package com.diegocunha.thenaapp.feature.feeding.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.diegocunha.thenaapp.feature.feeding.session.FeedingSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class FeedingTimerService : Service() {

    private val sessionManager: FeedingSessionManager by inject()
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
                    val elapsed = calculateElapsedSeconds(session)
                    updateNotification(formatElapsed(elapsed))
                    delay(1_000L)
                }
            }
        }
    }

    private fun calculateElapsedSeconds(session: com.diegocunha.thenaapp.feature.feeding.domain.model.ActiveFeedingSession): Long {
        val now = System.currentTimeMillis()
        val allSegments = session.leftSegments + session.rightSegments
        return allSegments.sumOf { segment ->
            val end = segment.endedAt ?: now
            (end - segment.startedAt) / 1_000L
        }
    }

    private fun formatElapsed(totalSeconds: Long): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    private fun updateNotification(elapsed: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(elapsed))
    }

    private fun buildNotification(elapsed: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle("Feeding in progress")
            .setContentText(elapsed)
            .setOngoing(true)
            .setSilent(true)
            .build()

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Feeding Timer",
            NotificationManager.IMPORTANCE_LOW,
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "feeding_timer"
    }
}