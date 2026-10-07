package com.focusflow.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_rooms")
data class RoomEntity(
    @PrimaryKey val id: String,
    val name: String,
    val hostUserId: String,
    val hostDisplayName: String,
    val subjectTag: String,
    val isPrivate: Boolean,
    val accessCode: String?,
    val targetDurationMinutes: Int,
    val participantCount: Int,
    val createdAtTimestamp: Long,
    val isJoined: Boolean = false,
)
