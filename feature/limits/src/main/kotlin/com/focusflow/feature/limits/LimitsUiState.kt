package com.focusflow.feature.limits

import com.focusflow.core.domain.blocking.model.AdvancedBlockingConfig
import com.focusflow.core.domain.blocking.model.BlockSchedule
import com.focusflow.core.domain.blocking.model.StudyChannel
import com.focusflow.core.domain.blocking.model.StudyModeConfig

data class AppLimitUiItem(
    val packageName: String,
    val appName: String,
    val isSafeListed: Boolean,
    val isEssential: Boolean,
    val blockDuringFocus: Boolean,
    val dailyTimeLimitMinutes: Int?,
    val dailyLaunchLimit: Int?,
)

data class LimitsUiState(
    val searchQuery: String = "",
    val apps: List<AppLimitUiItem> = emptyList(),
    val schedules: List<BlockSchedule> = emptyList(),
    val advancedBlockingConfig: AdvancedBlockingConfig = AdvancedBlockingConfig(),
    val studyModeConfig: StudyModeConfig = StudyModeConfig(),
    val studyChannels: List<StudyChannel> = emptyList(),
    val isAccessibilityEnabled: Boolean = false,
    val message: String? = null,
    val isLoading: Boolean = false,
)
