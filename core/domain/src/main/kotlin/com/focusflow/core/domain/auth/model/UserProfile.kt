package com.focusflow.core.domain.auth.model

/**
 * Domain representation of the user's gamification profile and streak state.
 */
data class UserProfile(
    val uid: String,
    val level: Int = 1,
    val xp: Long = 0L,
    val coins: Long = 0L,
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val totalFocusMinutes: Long = 0L,
)
