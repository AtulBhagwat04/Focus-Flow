package com.focusflow.core.data.auth

import com.focusflow.core.domain.auth.model.UserAccount
import com.focusflow.core.domain.auth.model.UserProfile
import com.focusflow.core.domain.auth.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor() : AuthRepository {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    override val currentUser: Flow<UserAccount?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser?.toDomain()
            trySend(user)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override val userProfile: Flow<UserProfile?> = currentUser.flatMapLatest { user ->
        if (user == null) {
            flowOf(null)
        } else {
            callbackFlow {
                val docRef = firestore.collection("users").document(user.uid)
                val registration = docRef.addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        val profile = UserProfile(
                            uid = user.uid,
                            level = snapshot.getLong("level")?.toInt() ?: 1,
                            xp = snapshot.getLong("xp") ?: 0L,
                            coins = snapshot.getLong("coins") ?: 0L,
                            currentStreakDays = snapshot.getLong("currentStreakDays")?.toInt() ?: 0,
                            longestStreakDays = snapshot.getLong("longestStreakDays")?.toInt() ?: 0,
                            totalFocusMinutes = snapshot.getLong("totalFocusMinutes") ?: 0L,
                        )
                        trySend(profile)
                    } else {
                        trySend(UserProfile(uid = user.uid))
                    }
                }
                awaitClose { registration.remove() }
            }
        }
    }

    override suspend fun signInAnonymously(): Result<UserAccount> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user?.toDomain()
                ?: return Result.failure(IllegalStateException("Anonymous user is null"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun linkGoogleAccount(idToken: String): Result<UserAccount> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(IllegalStateException("No current user to link"))
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = user.linkWithCredential(credential).await()
            val linkedUser = result.user?.toDomain()
                ?: return Result.failure(IllegalStateException("Linked user is null"))
            Result.success(linkedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(IllegalStateException("No current user to delete"))
            // Clean up user document in Firestore first
            firestore.collection("users").document(user.uid).delete().await()
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun FirebaseUser.toDomain() = UserAccount(
        uid = uid,
        email = email,
        displayName = displayName,
        isAnonymous = isAnonymous,
        photoUrl = photoUrl?.toString(),
    )
}
