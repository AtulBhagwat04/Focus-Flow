package com.focusflow.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val inviteCode: String,
    val status: String,
    val presenceState: String = "OFFLINE",
    val subjectTag: String? = null,
    val sessionRemainingSeconds: Long = 0L,
)
