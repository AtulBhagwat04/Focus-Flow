package com.focusflow.core.domain.social.model

data class Friend(
    val userId: String,
    val displayName: String,
    val inviteCode: String,
    val status: FriendStatus = FriendStatus.ACCEPTED,
    val currentPresence: UserPresence? = null,
)
