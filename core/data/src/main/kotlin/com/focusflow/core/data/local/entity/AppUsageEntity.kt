package com.focusflow.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity storing per-app daily usage, foreground duration, and launch counts.
 */
@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey
    val id: String,
    val epochDay: Long,
    val packageName: String,
    val appName: String,
    val totalTimeForegroundMs: Long,
    val launchCount: Int,
    val lastTimeUsedEpochMs: Long,
    val isSafeListed: Boolean,
)
