package com.focusflow.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_limits")
data class AppLimitEntity(
    @PrimaryKey
    val packageName: String,
    val dailyTimeLimitMs: Long?,
    val dailyLaunchLimit: Int?,
)
