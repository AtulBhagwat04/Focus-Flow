package com.focusflow.core.domain.blocking.model

data class StudyModeConfig(
    val isEnabled: Boolean = false,
    val allowlistedChannelIds: Set<String> = emptySet(),
    val allowlistedChannelNames: Set<String> = emptySet(),
    val failClosed: Boolean = false,
    val isKillSwitchActive: Boolean = false,
)
