package com.focusflow.core.domain.social.repository

import com.focusflow.core.domain.social.model.FocusRoom
import kotlinx.coroutines.flow.Flow

interface FocusRoomRepository {
    fun getPublicRooms(): Flow<List<FocusRoom>>
    fun getRoomById(roomId: String): Flow<FocusRoom?>
    suspend fun createRoom(room: FocusRoom): Result<String>
    suspend fun joinRoom(roomId: String, accessCode: String?): Result<Unit>
    suspend fun leaveRoom(roomId: String): Result<Unit>
    fun getActiveRoom(): Flow<FocusRoom?>
}
