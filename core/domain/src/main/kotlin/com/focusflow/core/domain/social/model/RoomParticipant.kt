package com.focusflow.core.domain.social.model

data class RoomParticipant(
    val userId: String,
    val displayName: String,
    val state: PresenceState = PresenceState.FOCUSING,
    val joinedAtTimestamp: Long = System.currentTimeMillis(),
)
