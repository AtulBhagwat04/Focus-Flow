package com.focusflow.core.domain.social.repository

import com.focusflow.core.domain.social.model.Friend
import kotlinx.coroutines.flow.Flow

interface FriendsRepository {
    fun getFriends(): Flow<List<Friend>>
    suspend fun sendFriendRequest(inviteCode: String): Result<Unit>
    suspend fun acceptFriendRequest(userId: String): Result<Unit>
    suspend fun removeFriend(userId: String): Result<Unit>
}
