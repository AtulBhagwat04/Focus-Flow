package com.focusflow.core.data.social

import com.focusflow.core.data.local.dao.SocialDao
import com.focusflow.core.data.local.entity.RoomEntity
import com.focusflow.core.domain.auth.repository.AuthRepository
import com.focusflow.core.domain.social.model.FocusRoom
import com.focusflow.core.domain.social.repository.FocusRoomRepository
import com.focusflow.core.domain.social.repository.PresenceRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreFocusRoomRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val socialDao: SocialDao,
    private val authRepository: AuthRepository,
    private val presenceRepository: PresenceRepository,
) : FocusRoomRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun getPublicRooms(): Flow<List<FocusRoom>> = callbackFlow {
        // Collect local cache first
        val localJob = scope.launch {
            socialDao.getAllRooms().collect { entities ->
                trySend(entities.map { it.toDomain() })
            }
        }

        // Firestore listener to sync active rooms
        val listener = firestore.collection("rooms")
            .whereEqualTo("isPrivate", false)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val rooms = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    RoomEntity(
                        id = doc.id,
                        name = name,
                        hostUserId = doc.getString("hostUserId") ?: "",
                        hostDisplayName = doc.getString("hostDisplayName") ?: "Host",
                        subjectTag = doc.getString("subjectTag") ?: "General",
                        isPrivate = doc.getBoolean("isPrivate") ?: false,
                        accessCode = doc.getString("accessCode"),
                        targetDurationMinutes = doc.getLong("targetDurationMinutes")?.toInt() ?: 25,
                        participantCount = doc.getLong("participantCount")?.toInt() ?: 1,
                        createdAtTimestamp = doc.getLong("createdAtTimestamp") ?: System.currentTimeMillis(),
                    )
                }
                scope.launch {
                    socialDao.insertRooms(rooms)
                }
            }

        awaitClose {
            localJob.cancel()
            listener.remove()
        }
    }

    override fun getRoomById(roomId: String): Flow<FocusRoom?> {
        return socialDao.getRoomById(roomId).map { it?.toDomain() }
    }

    override fun getActiveRoom(): Flow<FocusRoom?> {
        return socialDao.getActiveJoinedRoom().map { it?.toDomain() }
    }

    override suspend fun createRoom(room: FocusRoom): Result<String> = runCatching {
        val user = authRepository.currentUser.value
            ?: throw IllegalStateException("Must be signed in to create a room")

        val roomId = if (room.id.isNotBlank()) room.id else UUID.randomUUID().toString()
        val roomMap = hashMapOf(
            "name" to room.name,
            "hostUserId" to user.id,
            "hostDisplayName" to (user.displayName ?: "Host"),
            "subjectTag" to room.subjectTag,
            "isPrivate" to room.isPrivate,
            "accessCode" to room.accessCode,
            "targetDurationMinutes" to room.targetDurationMinutes,
            "participantCount" to 1,
            "createdAtTimestamp" to System.currentTimeMillis(),
        )

        firestore.collection("rooms").document(roomId).set(roomMap).await()

        val entity = RoomEntity(
            id = roomId,
            name = room.name,
            hostUserId = user.id,
            hostDisplayName = user.displayName ?: "Host",
            subjectTag = room.subjectTag,
            isPrivate = room.isPrivate,
            accessCode = room.accessCode,
            targetDurationMinutes = room.targetDurationMinutes,
            participantCount = 1,
            createdAtTimestamp = System.currentTimeMillis(),
            isJoined = true,
        )
        socialDao.leaveAllRooms()
        socialDao.insertRoom(entity)

        roomId
    }

    override suspend fun joinRoom(roomId: String, accessCode: String?): Result<Unit> = runCatching {
        val user = authRepository.currentUser.value
            ?: throw IllegalStateException("Must be signed in to join a room")

        val doc = firestore.collection("rooms").document(roomId).get().await()
        if (!doc.exists()) {
            throw IllegalArgumentException("Room does not exist")
        }

        val isPrivate = doc.getBoolean("isPrivate") ?: false
        val expectedCode = doc.getString("accessCode")
        if (isPrivate && !expectedCode.isNullOrBlank() && expectedCode != accessCode) {
            throw IllegalArgumentException("Invalid room passcode")
        }

        // Leave previous rooms locally and in presence
        socialDao.leaveAllRooms()
        socialDao.setRoomJoined(roomId, true)

        firestore.collection("rooms").document(roomId)
            .update("participantCount", FieldValue.increment(1))
            .await()
    }

    override suspend fun leaveRoom(roomId: String): Result<Unit> = runCatching {
        val user = authRepository.currentUser.value
        socialDao.setRoomJoined(roomId, false)

        if (user != null) {
            presenceRepository.leaveRoomPresence(roomId, user.id)
            runCatching {
                firestore.collection("rooms").document(roomId)
                    .update("participantCount", FieldValue.increment(-1))
                    .await()
            }
        }
    }

    private fun RoomEntity.toDomain() = FocusRoom(
        id = id,
        name = name,
        hostUserId = hostUserId,
        hostDisplayName = hostDisplayName,
        subjectTag = subjectTag,
        isPrivate = isPrivate,
        accessCode = accessCode,
        targetDurationMinutes = targetDurationMinutes,
        participantCount = participantCount,
        createdAtTimestamp = createdAtTimestamp,
    )
}
