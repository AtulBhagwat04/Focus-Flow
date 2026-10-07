package com.focusflow.core.domain.blocking.engine

import com.focusflow.core.domain.blocking.model.AdvancedBlockingConfig
import com.focusflow.core.domain.blocking.model.AppBlockRule
import com.focusflow.core.domain.blocking.model.BlockSchedule
import com.focusflow.core.domain.blocking.model.DailyAppLimit
import com.focusflow.core.domain.blocking.model.EmergencyPass
import com.focusflow.core.domain.blocking.model.StudyModeConfig
import com.focusflow.core.domain.model.SessionState

/**
 * Snapshot of all real-time signals supplied to [BlockingRulesEngine] to make a decision.
 */
data class BlockingSignals(
    val packageName: String,
    val nowEpochMs: Long,
    val dayOfWeek: Int,
    val minuteOfDay: Int,
    val sessionState: SessionState = SessionState.IDLE,
    val remainingSessionDurationMs: Long? = null,
    val consumedDurationTodayMs: Long = 0L,
    val launchCountToday: Int = 0,
    val emergencyPass: EmergencyPass = EmergencyPass(),
    val isDefaultLauncher: Boolean = false,
    val isDefaultDialer: Boolean = false,
    val userEssentials: Set<String> = emptySet(),
    val blockRule: AppBlockRule? = null,
    val dailyLimit: DailyAppLimit? = null,
    val activeSchedules: List<BlockSchedule> = emptyList(),
    val isShortFormFeedDetected: Boolean = false,
    val detectedChannelId: String? = null,
    val detectedChannelName: String? = null,
    val advancedBlockingConfig: AdvancedBlockingConfig? = null,
    val studyModeConfig: StudyModeConfig? = null,
)
