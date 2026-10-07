package com.focusflow.app.system.blocking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors screen interactive and keyguard states to pause blocking evaluation when display is off.
 */
@Singleton
class ScreenStateReceiver @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private val _isScreenInteractive = MutableStateFlow(powerManager?.isInteractive ?: true)
    val isScreenInteractive: StateFlow<Boolean> = _isScreenInteractive.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> _isScreenInteractive.value = false
                Intent.ACTION_SCREEN_ON,
                Intent.ACTION_USER_PRESENT -> _isScreenInteractive.value = true
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        context.registerReceiver(receiver, filter)
    }
}
