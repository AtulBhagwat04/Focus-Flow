package com.focusflow.core.domain.social.model

data class UserPresence(
    val userId: String,
    val displayName: String,
    val state: PresenceState = PresenceState.IDLE,
    val subjectTag: String? = null,
    val sessionRemainingSeconds: Long = 0L,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val isIncognito: Boolean = false,
)
