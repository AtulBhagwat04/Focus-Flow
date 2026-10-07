package com.focusflow.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_schedules")
data class BlockScheduleEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val daysOfWeekCsv: String,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
    val packageNamesCsv: String,
    val isEnabled: Boolean,
)
