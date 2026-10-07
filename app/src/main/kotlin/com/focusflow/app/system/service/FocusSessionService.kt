package com.focusflow.app.system.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.focusflow.app.system.notification.FocusNotificationManager
import com.focusflow.core.common.time.Clock
import com.focusflow.core.domain.audio.AmbientAudioPlayer
import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.SessionState
import com.focusflow.core.domain.repository.FocusSessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service hosting the active focus session.
 *
 * Rules:
 * - Owns live monitoring and persistent notifications during active sessions.
 * - Complies with Android 14+ foreground service type declarations (specialUse + mediaPlayback).
 * - Complies with Android 17 background audio restrictions by managing ambient sound playback.
 */
@AndroidEntryPoint
class FocusSessionService : Service() {

    @Inject lateinit var focusSessionRepository: FocusSessionRepository
    @Inject lateinit var clock: Clock
    @Inject lateinit var notificationManager: FocusNotificationManager
    @Inject lateinit var ambientAudioPlayer: AmbientAudioPlayer

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var tickerJob: Job? = null
    private var isForegroundActive = false

    override fun onCreate() {
        super.onCreate()
        observeSession()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> serviceScope.launch { focusSessionRepository.pauseSession() }
            ACTION_RESUME -> serviceScope.launch { focusSessionRepository.resumeSession() }
            ACTION_STOP -> serviceScope.launch { focusSessionRepository.completeSession() }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun observeSession() {
        serviceScope.launch {
            focusSessionRepository.currentSession.collectLatest { session ->
                if (session != null && session.state.isActive) {
                    handleActiveSession(session)
                } else {
                    handleNoActiveSession()
                }
            }
        }
    }

    private fun handleActiveSession(session: FocusSession) {
        val notification = notificationManager.buildNotification(session, clock.now())
        if (!isForegroundActive) {
            val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE or
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            }
            ServiceCompat.startForeground(
                this,
                FocusNotificationManager.NOTIFICATION_ID,
                notification,
                serviceType,
            )
            isForegroundActive = true
        } else {
            val systemNotificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
            systemNotificationManager.notify(FocusNotificationManager.NOTIFICATION_ID, notification)
        }

        if (session.state == SessionState.RUNNING) {
            startTicker(session)
        } else {
            stopTicker()
            ambientAudioPlayer.pause()
        }
    }

    private fun handleNoActiveSession() {
        stopTicker()
        ambientAudioPlayer.stop()
        if (isForegroundActive) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            isForegroundActive = false
        }
        stopSelf()
    }

    private fun startTicker(session: FocusSession) {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            val systemNotificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
            while (isActive) {
                delay(TICK_INTERVAL_MS)
                val currentNow = clock.now()
                // Auto-complete countdown session when target is reached
                if (session.isTargetReached(currentNow)) {
                    focusSessionRepository.completeSession()
                    break
                }
                val updatedNotification = notificationManager.buildNotification(session, currentNow)
                systemNotificationManager.notify(FocusNotificationManager.NOTIFICATION_ID, updatedNotification)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTicker()
        ambientAudioPlayer.release()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "com.focusflow.action.START"
        const val ACTION_PAUSE = "com.focusflow.action.PAUSE"
        const val ACTION_RESUME = "com.focusflow.action.RESUME"
        const val ACTION_STOP = "com.focusflow.action.STOP"

        private const val TICK_INTERVAL_MS = 1000L
    }
}
