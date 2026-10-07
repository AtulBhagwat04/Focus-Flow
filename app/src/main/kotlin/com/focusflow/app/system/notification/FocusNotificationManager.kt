package com.focusflow.app.system.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.focusflow.app.MainActivity
import com.focusflow.app.R
import com.focusflow.app.system.service.FocusSessionService
import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.core.domain.model.SessionState
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Active Focus Session",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Shows timer and controls for active focus sessions"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(session: FocusSession, currentEpochMs: Long): Notification {
        val contentPendingIntent = buildContentIntent()
        val title = if (session.state == SessionState.PAUSED) "Focus Session Paused" else "Focus Session Active"
        val text = buildStatusText(session, currentEpochMs)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        addSessionActions(builder, session)
        return builder.build()
    }

    private fun buildContentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_APP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun buildStatusText(session: FocusSession, currentEpochMs: Long): String =
        if (session.mode is SessionMode.Stopwatch) {
            "${formatDuration(session.elapsedDurationMs(currentEpochMs))} focused"
        } else {
            "${formatDuration(session.remainingDurationMs(currentEpochMs))} remaining"
        }

    private fun addSessionActions(builder: NotificationCompat.Builder, session: FocusSession) {
        when (session.state) {
            SessionState.RUNNING -> builder.addAction(
                0, "Pause", buildServiceIntent(FocusSessionService.ACTION_PAUSE, REQUEST_CODE_PAUSE)
            )
            SessionState.PAUSED -> builder.addAction(
                0, "Resume", buildServiceIntent(FocusSessionService.ACTION_RESUME, REQUEST_CODE_RESUME)
            )
            else -> Unit
        }
        builder.addAction(0, "Finish", buildServiceIntent(FocusSessionService.ACTION_STOP, REQUEST_CODE_STOP))
    }

    private fun buildServiceIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, FocusSessionService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = (durationMs / MILLIS_PER_SECOND).coerceAtLeast(0L)
        val minutes = totalSeconds / SECONDS_PER_MINUTE
        val seconds = totalSeconds % SECONDS_PER_MINUTE
        val hours = minutes / MINUTES_PER_HOUR

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes % MINUTES_PER_HOUR, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    companion object {
        const val CHANNEL_ID = "focus_session_channel"
        const val NOTIFICATION_ID = 1001

        private const val REQUEST_CODE_OPEN_APP = 0
        private const val REQUEST_CODE_PAUSE = 1
        private const val REQUEST_CODE_RESUME = 2
        private const val REQUEST_CODE_STOP = 3

        private const val MILLIS_PER_SECOND = 1000L
        private const val SECONDS_PER_MINUTE = 60L
        private const val MINUTES_PER_HOUR = 60L
    }
}
