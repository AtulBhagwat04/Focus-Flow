package com.focusflow.core.domain.blocking.model

/**
 * Model representing the status of an emergency bypass pass.
 */
data class EmergencyPass(
    val expiresAtEpochMs: Long = 0L,
    val passesUsedToday: Int = 0,
    val maxPassesPerDay: Int = 3,
) {
    fun isActiveAt(nowEpochMs: Long): Boolean = nowEpochMs < expiresAtEpochMs

    val hasPassesRemaining: Boolean
        get() = passesUsedToday < maxPassesPerDay
}
