package com.focusflow.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity storing daily aggregated screen time, focus time, and device unlocks.
 */
@Entity(tableName = "daily_usage")
data class DailyUsageEntity(
    @PrimaryKey
    val epochDay: Long,
    val totalScreenTimeMs: Long,
    val totalFocusTimeMs: Long,
    val unlockCount: Int,
    val updatedAtEpochMs: Long,
)
