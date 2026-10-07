package com.focusflow.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity storing user-designated essential packages (immune to app blocking).
 */
@Entity(tableName = "user_essential_apps")
data class UserEssentialAppEntity(
    @PrimaryKey
    val packageName: String,
    val addedAtEpochMs: Long,
)
