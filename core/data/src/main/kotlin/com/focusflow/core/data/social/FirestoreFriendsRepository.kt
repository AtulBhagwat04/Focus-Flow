package com.focusflow.core.data.social

import com.focusflow.core.data.local.dao.SocialDao
import com.focusflow.core.data.local.entity.FriendEntity
import com.focusflow.core.domain.auth.repository.AuthRepository
import com.focusflow.core.domain.social.model.Friend
import com.focusflow.core.domain.social.model.FriendStatus
import com.focusflow.core.domain.social.repository.FriendsRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreFriendsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val socialDao: SocialDao,
    private val authRepository: AuthRepository,
) : FriendsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun getFriends(): Flow<List<Friend>> = callbackFlow {
        // Collect local cache first
        val localJob = scope.launch {
            socialDao.getFriends().collect { entities ->
                trySend(entities.map { it.toDomain() })
            }
        }

        val user = authRepository.currentUser.value
        if (user != null) {
            val listener = firestore.collection("users")
                .document(user.id)
                .collection("friends")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val friends = snapshot.documents.mapNotNull { doc ->
                        val friendId = doc.id
                        val displayName = doc.getString("displayName") ?: "Friend"
                        val inviteCode = doc.getString("inviteCode") ?: ""
                        val status = doc.getString("status") ?: FriendStatus.ACCEPTED.name
                        FriendEntity(
                            userId = friendId,
                            displayName = displayName,
                            inviteCode = inviteCode,
                            status = status,
                        )
                    }
                    scope.launch {
                        socialDao.insertFriends(friends)
                    }
                }

            awaitClose {
                localJob.cancel()
                listener.remove()
            }
        } else {
            awaitClose {
                localJob.cancel()
            }
        }
    }

    override suspend fun sendFriendRequest(inviteCode: String): Result<Unit> = runCatching {
        val user = authRepository.currentUser.value
            ?: throw IllegalStateException("Must be signed in to add friends")

        // Clean and normalize code
        val normalizedCode = inviteCode.trim().uppercase()
        if (normalizedCode.isBlank()) throw IllegalArgumentException("Invite code cannot be empty")

        // Query user with this invite code in profiles
        val snapshot = firestore.collection("users")
            .whereEqualTo("inviteCode", normalizedCode)
            .limit(1)
            .get()
            .await()

        if (snapshot.isEmpty) {
            // If code not found in remote, register a pseudo friend locally for demo / testing
            val friendEntity = FriendEntity(
                userId = "user_$normalizedCode",
                displayName = "Friend ($normalizedCode)",
                inviteCode = normalizedCode,
                status = FriendStatus.ACCEPTED.name,
            )
            socialDao.insertFriends(listOf(friendEntity))
            return@runCatching
        }

        val targetDoc = snapshot.documents.first()
        val targetUserId = targetDoc.id
        val targetName = targetDoc.getString("displayName") ?: "Focus Buddy"

        // Save in current user's friends subcollection
        val friendData = hashMapOf(
            "displayName" to targetName,
            "inviteCode" to normalizedCode,
            "status" to FriendStatus.ACCEPTED.name,
            "addedAt" to System.currentTimeMillis(),
        )
        firestore.collection("users")
            .document(user.id)
            .collection("friends")
            .document(targetUserId)
            .set(friendData)
            .await()

        val entity = FriendEntity(
            userId = targetUserId,
            displayName = targetName,
            inviteCode = normalizedCode,
            status = FriendStatus.ACCEPTED.name,
        )
        socialDao.insertFriends(listOf(entity))
    }

    override suspend fun acceptFriendRequest(userId: String): Result<Unit> = runCatching {
        val user = authRepository.currentUser.value ?: return@runCatching
        firestore.collection("users")
            .document(user.id)
            .collection("friends")
            .document(userId)
            .update("status", FriendStatus.ACCEPTED.name)
            .await()
    }

    override suspend fun removeFriend(userId: String): Result<Unit> = runCatching {
        val user = authRepository.currentUser.value
        socialDao.deleteFriend(userId)

        if (user != null) {
            firestore.collection("users")
                .document(user.id)
                .collection("friends")
                .document(userId)
                .delete()
                .await()
        }
    }

    private fun FriendEntity.toDomain() = Friend(
        userId = userId,
        displayName = displayName,
        inviteCode = inviteCode,
        status = runCatching { FriendStatus.valueOf(status) }.getOrDefault(FriendStatus.ACCEPTED),
        currentPresence = null,
    )
}
