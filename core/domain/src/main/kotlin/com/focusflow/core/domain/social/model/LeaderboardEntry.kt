package com.focusflow.core.domain.social.model

data class LeaderboardEntry(
    val userId: String,
    val displayName: String,
    val rank: Int,
    val totalFocusMinutes: Long,
    val streakDays: Int,
    val xp: Long,
    val isCurrentUser: Boolean = false,
)
