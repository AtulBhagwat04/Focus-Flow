package com.focusflow.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_pass_state")
data class EmergencyPassEntity(
    @PrimaryKey
    val id: Int = 1,
    val expiresAtEpochMs: Long,
    val passesUsedToday: Int,
    val dateEpochDay: Long,
)
