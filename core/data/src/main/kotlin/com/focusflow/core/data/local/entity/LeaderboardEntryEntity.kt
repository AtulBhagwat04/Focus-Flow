package com.focusflow.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntryEntity(
    @PrimaryKey val id: String,
    val scope: String,
    val userId: String,
    val displayName: String,
    val rank: Int,
    val totalFocusMinutes: Long,
    val streakDays: Int,
    val xp: Long,
    val isCurrentUser: Boolean,
)
