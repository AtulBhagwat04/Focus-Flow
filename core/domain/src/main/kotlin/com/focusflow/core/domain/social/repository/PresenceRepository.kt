package com.focusflow.core.domain.social.repository

import com.focusflow.core.domain.social.model.RoomParticipant
import com.focusflow.core.domain.social.model.UserPresence
import kotlinx.coroutines.flow.Flow

interface PresenceRepository {
    suspend fun updatePresence(presence: UserPresence): Result<Unit>
    fun observeUserPresence(userId: String): Flow<UserPresence?>
    fun observeRoomPresence(roomId: String): Flow<List<RoomParticipant>>
    suspend fun setIncognito(incognito: Boolean): Result<Unit>
    suspend fun leaveRoomPresence(roomId: String, userId: String): Result<Unit>
}
