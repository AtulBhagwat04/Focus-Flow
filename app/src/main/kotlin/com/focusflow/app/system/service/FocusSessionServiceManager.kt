package com.focusflow.app.system.service

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.focusflow.core.domain.repository.FocusSessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusSessionServiceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val focusSessionRepository: FocusSessionRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isObserving = false
    private var isServiceRunning = false

    fun startObserving() {
        if (isObserving) return
        isObserving = true

        scope.launch {
            focusSessionRepository.currentSession.collectLatest { session ->
                if (session != null && session.state.isActive) {
                    startForegroundService()
                } else {
                    stopForegroundService()
                }
            }
        }
    }

    private fun startForegroundService() {
        val intent = Intent(context, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_START
        }
        try {
            ContextCompat.startForegroundService(context, intent)
            isServiceRunning = true
        } catch (_: Exception) {
            // Degrades gracefully on restricted OEM battery modes or background execution limits
        }
    }

    private fun stopForegroundService() {
        if (!isServiceRunning) return
        val intent = Intent(context, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_STOP
        }
        try {
            context.stopService(intent)
        } catch (_: Exception) {
            // Safe cleanup
        } finally {
            isServiceRunning = false
        }
    }
}
