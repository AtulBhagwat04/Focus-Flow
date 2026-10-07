package com.focusflow.core.data.social

import com.focusflow.core.data.local.dao.SocialDao
import com.focusflow.core.data.local.entity.LeaderboardEntryEntity
import com.focusflow.core.domain.auth.repository.AuthRepository
import com.focusflow.core.domain.social.model.LeaderboardEntry
import com.focusflow.core.domain.social.model.LeaderboardScope
import com.focusflow.core.domain.social.repository.LeaderboardRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreLeaderboardRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val socialDao: SocialDao,
    private val authRepository: AuthRepository,
) : LeaderboardRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    private fun getCurrentWeekId(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val week = calendar.get(Calendar.WEEK_OF_YEAR)
        return "${year}_W${week}"
    }

    override fun getWeeklyLeaderboard(scopeType: LeaderboardScope): Flow<List<LeaderboardEntry>> = callbackFlow {
        // Collect local cache first
        val localJob = scope.launch {
            socialDao.getLeaderboard(scopeType.name).collect { entities ->
                if (entities.isNotEmpty()) {
                    trySend(entities.map { it.toDomain() })
                } else {
                    // Seed initial starter leaderboard if local cache is empty
                    seedDefaultLeaderboard(scopeType)
                }
            }
        }

        val weekId = getCurrentWeekId()
        val currentUserId = authRepository.currentUser.value?.id

        val listener = firestore.collection("leaderboards")
            .document("${scopeType.name.lowercase()}_$weekId")
            .collection("entries")
            .orderBy("totalFocusMinutes", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) return@addSnapshotListener
                val entries = snapshot.documents.mapIndexed { index, doc ->
                    val userId = doc.id
                    val displayName = doc.getString("displayName") ?: "Focus Achiever"
                    val focusMinutes = doc.getLong("totalFocusMinutes") ?: 0L
                    val streakDays = doc.getLong("streakDays")?.toInt() ?: 0
                    val xp = doc.getLong("xp") ?: 0L

                    LeaderboardEntryEntity(
                        id = "${scopeType.name}_$userId",
                        scope = scopeType.name,
                        userId = userId,
                        displayName = displayName,
                        rank = index + 1,
                        totalFocusMinutes = focusMinutes,
                        streakDays = streakDays,
                        xp = xp,
                        isCurrentUser = userId == currentUserId,
                    )
                }

                scope.launch {
                    socialDao.clearLeaderboard(scopeType.name)
                    socialDao.insertLeaderboard(entries)
                }
            }

        awaitClose {
            localJob.cancel()
            listener.remove()
        }
    }

    override suspend fun refreshLeaderboard(scope: LeaderboardScope): Result<Unit> = runCatching {
        val weekId = getCurrentWeekId()
        val currentUserId = authRepository.currentUser.value?.id
        val snapshot = firestore.collection("leaderboards")
            .document("${scope.name.lowercase()}_$weekId")
            .collection("entries")
            .orderBy("totalFocusMinutes", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .await()

        if (!snapshot.isEmpty) {
            val entries = snapshot.documents.mapIndexed { index, doc ->
                val userId = doc.id
                val displayName = doc.getString("displayName") ?: "Focus Achiever"
                val focusMinutes = doc.getLong("totalFocusMinutes") ?: 0L
                val streakDays = doc.getLong("streakDays")?.toInt() ?: 0
                val xp = doc.getLong("xp") ?: 0L

                LeaderboardEntryEntity(
                    id = "${scope.name}_$userId",
                    scope = scope.name,
                    userId = userId,
                    displayName = displayName,
                    rank = index + 1,
                    totalFocusMinutes = focusMinutes,
                    streakDays = streakDays,
                    xp = xp,
                    isCurrentUser = userId == currentUserId,
                )
            }
            socialDao.clearLeaderboard(scope.name)
            socialDao.insertLeaderboard(entries)
        }
    }

    private suspend fun seedDefaultLeaderboard(scopeType: LeaderboardScope) {
        val currentUserId = authRepository.currentUser.value?.id ?: "me"
        val userProfile = authRepository.userProfile.value
        val starterEntries = listOf(
            LeaderboardEntryEntity(
                id = "${scopeType.name}_starter_1",
                scope = scopeType.name,
                userId = "starter_1",
                displayName = "Aarav S.",
                rank = 1,
                totalFocusMinutes = 1420L,
                streakDays = 14,
                xp = 2840L,
                isCurrentUser = false,
            ),
            LeaderboardEntryEntity(
                id = "${scopeType.name}_starter_2",
                scope = scopeType.name,
                userId = "starter_2",
                displayName = "Priya M.",
                rank = 2,
                totalFocusMinutes = 1180L,
                streakDays = 9,
                xp = 2360L,
                isCurrentUser = false,
            ),
            LeaderboardEntryEntity(
                id = "${scopeType.name}_$currentUserId",
                scope = scopeType.name,
                userId = currentUserId,
                displayName = authRepository.currentUser.value?.displayName ?: "You",
                rank = 3,
                totalFocusMinutes = 840L,
                streakDays = userProfile?.currentStreakDays ?: 3,
                xp = userProfile?.xp ?: 1680L,
                isCurrentUser = true,
            ),
            LeaderboardEntryEntity(
                id = "${scopeType.name}_starter_4",
                scope = scopeType.name,
                userId = "starter_4",
                displayName = "Dev K.",
                rank = 4,
                totalFocusMinutes = 620L,
                streakDays = 5,
                xp = 1240L,
                isCurrentUser = false,
            ),
        )
        socialDao.insertLeaderboard(starterEntries)
    }

    private fun LeaderboardEntryEntity.toDomain() = LeaderboardEntry(
        userId = userId,
        displayName = displayName,
        rank = rank,
        totalFocusMinutes = totalFocusMinutes,
        streakDays = streakDays,
        xp = xp,
        isCurrentUser = isCurrentUser,
    )
}
