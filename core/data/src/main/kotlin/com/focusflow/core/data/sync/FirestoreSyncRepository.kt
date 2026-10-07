package com.focusflow.core.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.focusflow.core.data.local.dao.FocusSessionDao
import com.focusflow.core.data.local.dao.SubjectTagDao
import com.focusflow.core.data.local.dao.UsageDao
import com.focusflow.core.domain.auth.repository.AuthRepository
import com.focusflow.core.domain.sync.model.SyncResult
import com.focusflow.core.domain.sync.repository.SyncRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val SYNC_WORK_NAME = "focus_flow_cloud_sync"
private const val EXPORT_SESSION_LIMIT = 500

@Singleton
class FirestoreSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val focusSessionDao: FocusSessionDao,
    private val subjectTagDao: SubjectTagDao,
    private val usageDao: UsageDao,
    private val authRepository: AuthRepository,
) : SyncRepository {

    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: Flow<Boolean> = _isSyncing.asStateFlow()

    override fun enqueueSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    override suspend fun performSync(): SyncResult {
        val user = authRepository.currentUser.first() ?: return SyncResult.Success
        _isSyncing.value = true

        return try {
            val sessions = focusSessionDao.getHistoricalSessions(100).first()
            val batch = firestore.batch()
            var totalFocusMs = 0L

            for (session in sessions) {
                val docRef = firestore.collection("users")
                    .document(user.uid)
                    .collection("sessions")
                    .document(session.id)

                val data = mapOf(
                    "id" to session.id,
                    "state" to session.state,
                    "tagId" to session.tagId,
                    "targetDurationMs" to session.targetDurationMs,
                    "startedAtEpochMs" to session.startedAtEpochMs,
                    "completedAtEpochMs" to session.completedAtEpochMs,
                    "totalPausedDurationMs" to session.totalPausedDurationMs,
                )
                batch.set(docRef, data, SetOptions.merge())

                val duration = (session.completedAtEpochMs ?: session.startedAtEpochMs) -
                    session.startedAtEpochMs - session.totalPausedDurationMs
                if (duration > 0) totalFocusMs += duration
            }

            batch.commit().await()

            // Update user summary doc
            val totalMinutes = totalFocusMs / 60_000L
            val userDocRef = firestore.collection("users").document(user.uid)
            userDocRef.set(
                mapOf(
                    "uid" to user.uid,
                    "totalFocusMinutes" to totalMinutes,
                    "lastSyncedEpochMs" to System.currentTimeMillis(),
                ),
                SetOptions.merge(),
            ).await()

            SyncResult.Success
        } catch (e: Exception) {
            SyncResult.Failure(e.message ?: "Sync failed")
        } finally {
            _isSyncing.value = false
        }
    }

    override suspend fun exportUserDataJson(): Result<String> {
        return try {
            val sessions = focusSessionDao.getHistoricalSessions(EXPORT_SESSION_LIMIT).first()
            val tags = subjectTagDao.observeAllTags().first()
            val user = authRepository.currentUser.first()

            val sessionsJson = sessions.joinToString(",", "[", "]") { s ->
                """{"id":"${s.id}","state":"${s.state}","startedAt":${s.startedAtEpochMs}}"""
            }
            val tagsJson = tags.joinToString(",", "[", "]") { t ->
                """{"id":"${t.id}","name":"${t.name}","color":"${t.colorHex}"}"""
            }

            val export = """
            {
              "exportTimestamp": ${System.currentTimeMillis()},
              "userId": "${user?.uid ?: "anonymous"}",
              "tags": $tagsJson,
              "sessions": $sessionsJson
            }
            """.trimIndent()

            Result.success(export)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
