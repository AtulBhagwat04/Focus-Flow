package com.focusflow.core.data.social

import com.focusflow.core.domain.auth.repository.AuthRepository
import com.focusflow.core.domain.social.model.PresenceState
import com.focusflow.core.domain.social.model.RoomParticipant
import com.focusflow.core.domain.social.model.UserPresence
import com.focusflow.core.domain.social.repository.PresenceRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePresenceRepository @Inject constructor(
    private val authRepository: AuthRepository,
) : PresenceRepository {

    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    private var isIncognitoMode: Boolean = false

    override suspend fun updatePresence(presence: UserPresence): Result<Unit> = runCatching {
        val user = authRepository.currentUser.value ?: return@runCatching
        if (isIncognitoMode || presence.isIncognito) {
            val incognitoPresence = mapOf(
                "userId" to user.id,
                "displayName" to (user.displayName ?: "FocusFlow Member"),
                "state" to PresenceState.IDLE.name,
                "subjectTag" to null,
                "sessionRemainingSeconds" to 0L,
                "lastSeenTimestamp" to System.currentTimeMillis(),
                "isIncognito" to true,
            )
            val ref = database.getReference("presence").child(user.id)
            ref.setValue(incognitoPresence).await()
            ref.onDisconnect().setValue(
                incognitoPresence + ("state" to PresenceState.OFFLINE.name)
            )
            return@runCatching
        }

        val presenceMap = mapOf(
            "userId" to presence.userId,
            "displayName" to presence.displayName,
            "state" to presence.state.name,
            "subjectTag" to presence.subjectTag,
            "sessionRemainingSeconds" to presence.sessionRemainingSeconds,
            "lastSeenTimestamp" to presence.lastSeenTimestamp,
            "isIncognito" to false,
        )

        val ref = database.getReference("presence").child(user.id)
        ref.setValue(presenceMap).await()
        ref.onDisconnect().setValue(
            presenceMap + ("state" to PresenceState.OFFLINE.name)
        )
    }

    override fun observeUserPresence(userId: String): Flow<UserPresence?> = callbackFlow {
        val ref = database.getReference("presence").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(null)
                    return
                }
                val stateName = snapshot.child("state").getValue(String::class.java) ?: PresenceState.OFFLINE.name
                val state = runCatching { PresenceState.valueOf(stateName) }.getOrDefault(PresenceState.OFFLINE)
                val presence = UserPresence(
                    userId = userId,
                    displayName = snapshot.child("displayName").getValue(String::class.java) ?: "Member",
                    state = state,
                    subjectTag = snapshot.child("subjectTag").getValue(String::class.java),
                    sessionRemainingSeconds = snapshot.child("sessionRemainingSeconds").getValue(Long::class.java) ?: 0L,
                    lastSeenTimestamp = snapshot.child("lastSeenTimestamp").getValue(Long::class.java) ?: 0L,
                    isIncognito = snapshot.child("isIncognito").getValue(Boolean::class.java) ?: false,
                )
                trySend(presence)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(null)
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun observeRoomPresence(roomId: String): Flow<List<RoomParticipant>> = callbackFlow {
        val ref = database.getReference("room_presence").child(roomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val participants = mutableListOf<RoomParticipant>()
                for (child in snapshot.children) {
                    val userId = child.key ?: continue
                    val displayName = child.child("displayName").getValue(String::class.java) ?: "Member"
                    val stateName = child.child("state").getValue(String::class.java) ?: PresenceState.FOCUSING.name
                    val state = runCatching { PresenceState.valueOf(stateName) }.getOrDefault(PresenceState.FOCUSING)
                    val joinedAt = child.child("joinedAtTimestamp").getValue(Long::class.java) ?: System.currentTimeMillis()

                    participants.add(
                        RoomParticipant(
                            userId = userId,
                            displayName = displayName,
                            state = state,
                            joinedAtTimestamp = joinedAt,
                        )
                    )
                }
                trySend(participants)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun setIncognito(incognito: Boolean): Result<Unit> = runCatching {
        isIncognitoMode = incognito
        val user = authRepository.currentUser.value
        if (user != null) {
            updatePresence(
                UserPresence(
                    userId = user.id,
                    displayName = user.displayName ?: "Member",
                    isIncognito = incognito,
                )
            )
        }
    }

    override suspend fun leaveRoomPresence(roomId: String, userId: String): Result<Unit> = runCatching {
        database.getReference("room_presence")
            .child(roomId)
            .child(userId)
            .removeValue()
            .await()
    }
}
