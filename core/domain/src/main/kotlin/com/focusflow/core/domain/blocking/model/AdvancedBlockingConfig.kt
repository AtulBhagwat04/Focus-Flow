package com.focusflow.core.domain.blocking.model

data class AdvancedBlockingConfig(
    val isShortsBlockingEnabled: Boolean = false,
    val isReelsBlockingEnabled: Boolean = false,
    val blockDuringFocusOnly: Boolean = true,
    val isKillSwitchActive: Boolean = false,
)
