package com.focusflow.core.domain.social.model

data class FocusRoom(
    val id: String,
    val name: String,
    val hostUserId: String,
    val hostDisplayName: String,
    val subjectTag: String,
    val isPrivate: Boolean = false,
    val accessCode: String? = null,
    val targetDurationMinutes: Int = 25,
    val participantCount: Int = 1,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
)
