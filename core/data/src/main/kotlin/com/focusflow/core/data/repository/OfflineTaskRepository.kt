package com.focusflow.core.data.repository

import com.focusflow.core.data.local.dao.TaskDao
import com.focusflow.core.data.local.entity.TaskEntity
import com.focusflow.core.data.local.entity.asDomainModel
import com.focusflow.core.data.local.entity.asEntity
import com.focusflow.core.domain.auth.repository.AuthRepository
import com.focusflow.core.domain.model.FocusTask
import com.focusflow.core.domain.repository.TaskRepository
import com.focusflow.core.domain.sync.repository.SyncRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class OfflineTaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val syncRepositoryProvider: Provider<SyncRepository>,
) : TaskRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var firestoreListener: ListenerRegistration? = null
    private var currentUserId: String? = null

    init {
        scope.launch {
            authRepository.currentUser.collect { user ->
                val newUid = user?.uid
                if (newUid != currentUserId) {
                    firestoreListener?.remove()
                    firestoreListener = null
                    currentUserId = newUid
                    if (newUid != null) {
                        attachFirestoreListener(newUid)
                    }
                }
            }
        }
    }

    override val tasks: Flow<List<FocusTask>> =
        taskDao.getTasks().map { entities -> entities.map { it.asDomainModel() } }

    override val activeTask: Flow<FocusTask?> =
        taskDao.getActiveTask().map { it?.asDomainModel() }

    override suspend fun getTasks(): List<FocusTask> =
        taskDao.getAllTasks().map { it.asDomainModel() }

    override suspend fun addTask(title: String, durationMinutes: Int): FocusTask {
        val now = System.currentTimeMillis()
        val newTask = FocusTask(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            durationMinutes = durationMinutes,
            completedSeconds = 0,
            isCompleted = false,
            isCurrentFocus = false,
        )
        val entity = newTask.asEntity(createdAtEpochMs = now, updatedAtEpochMs = now)
        taskDao.upsertTask(entity)
        syncTaskToFirestore(entity)
        return newTask
    }

    override suspend fun toggleTaskCompleted(taskId: String): FocusTask? {
        val current = taskDao.getTaskById(taskId) ?: return null
        val newCompleted = !current.isCompleted
        val newCompletedSec = if (newCompleted) current.durationMinutes * 60 else 0
        val updated = current.copy(
            isCompleted = newCompleted,
            completedSeconds = newCompletedSec,
            isCurrentFocus = if (newCompleted) false else current.isCurrentFocus,
            updatedAtEpochMs = System.currentTimeMillis(),
        )
        taskDao.upsertTask(updated)
        syncTaskToFirestore(updated)
        return updated.asDomainModel()
    }

    override suspend fun selectTaskForFocus(taskId: String): FocusTask? {
        val current = taskDao.getTaskById(taskId) ?: return null
        taskDao.setActiveTask(taskId)
        val updated = current.copy(
            isCurrentFocus = true,
            updatedAtEpochMs = System.currentTimeMillis(),
        )
        syncTaskToFirestore(updated)
        return updated.asDomainModel()
    }

    override suspend fun recordFocusProgress(taskId: String, additionalSeconds: Int): FocusTask? {
        val current = taskDao.getTaskById(taskId) ?: return null
        val maxSec = current.durationMinutes * 60
        val newSec = (current.completedSeconds + additionalSeconds).coerceAtMost(maxSec)
        val isDone = newSec >= maxSec
        val updated = current.copy(
            completedSeconds = newSec,
            isCompleted = current.isCompleted || isDone,
            isCurrentFocus = if (isDone) false else current.isCurrentFocus,
            updatedAtEpochMs = System.currentTimeMillis(),
        )
        taskDao.upsertTask(updated)
        syncTaskToFirestore(updated)
        return updated.asDomainModel()
    }

    override suspend fun completeTask(taskId: String): FocusTask? {
        val current = taskDao.getTaskById(taskId) ?: return null
        val updated = current.copy(
            isCompleted = true,
            completedSeconds = current.durationMinutes * 60,
            isCurrentFocus = false,
            updatedAtEpochMs = System.currentTimeMillis(),
        )
        taskDao.upsertTask(updated)
        syncTaskToFirestore(updated)
        return updated.asDomainModel()
    }

    override suspend fun clearActiveTask() {
        val active = taskDao.getActiveTask().firstOrNull()
        taskDao.clearActiveTask()
        if (active != null) {
            val updated = active.copy(isCurrentFocus = false, updatedAtEpochMs = System.currentTimeMillis())
            syncTaskToFirestore(updated)
        }
    }

    override suspend fun deleteTask(taskId: String) {
        taskDao.deleteTask(taskId)
        deleteTaskFromFirestore(taskId)
    }

    private fun attachFirestoreListener(userId: String) {
        // First sync any existing local tasks to Firestore
        scope.launch {
            runCatching {
                val localTasks = taskDao.getAllTasks()
                if (localTasks.isNotEmpty()) {
                    val batch = firestore.batch()
                    for (task in localTasks) {
                        val docRef = firestore.collection("users")
                            .document(userId)
                            .collection("tasks")
                            .document(task.id)
                        val data = mapOf(
                            "id" to task.id,
                            "title" to task.title,
                            "durationMinutes" to task.durationMinutes,
                            "completedSeconds" to task.completedSeconds,
                            "isCompleted" to task.isCompleted,
                            "isCurrentFocus" to task.isCurrentFocus,
                            "createdAtEpochMs" to task.createdAtEpochMs,
                            "updatedAtEpochMs" to task.updatedAtEpochMs,
                        )
                        batch.set(docRef, data, SetOptions.merge())
                    }
                    batch.commit().await()
                }
            }
        }

        // Listen for cloud changes and update Room
        firestoreListener = firestore.collection("users")
            .document(userId)
            .collection("tasks")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val entities = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val durationMinutes = doc.getLong("durationMinutes")?.toInt() ?: 25
                    val completedSeconds = doc.getLong("completedSeconds")?.toInt() ?: 0
                    val isCompleted = doc.getBoolean("isCompleted") ?: false
                    val isCurrentFocus = doc.getBoolean("isCurrentFocus") ?: false
                    val createdAtEpochMs = doc.getLong("createdAtEpochMs") ?: System.currentTimeMillis()
                    val updatedAtEpochMs = doc.getLong("updatedAtEpochMs") ?: System.currentTimeMillis()

                    TaskEntity(
                        id = id,
                        title = title,
                        durationMinutes = durationMinutes,
                        completedSeconds = completedSeconds,
                        isCompleted = isCompleted,
                        isCurrentFocus = isCurrentFocus,
                        createdAtEpochMs = createdAtEpochMs,
                        updatedAtEpochMs = updatedAtEpochMs,
                    )
                }
                if (entities.isNotEmpty()) {
                    scope.launch {
                        for (remote in entities) {
                            val local = taskDao.getTaskById(remote.id)
                            if (local == null || remote.updatedAtEpochMs >= local.updatedAtEpochMs) {
                                taskDao.upsertTask(remote)
                            }
                        }
                    }
                }
            }
    }

    private fun syncTaskToFirestore(entity: TaskEntity) {
        // Enqueue WorkManager job with network constraint (fires automatically when internet connects)
        runCatching {
            syncRepositoryProvider.get().enqueueSync()
        }

        // Direct Firestore write (queued into Firestore offline cache if offline)
        val uid = authRepository.currentUser.value?.uid ?: return
        scope.launch {
            runCatching {
                val data = mapOf(
                    "id" to entity.id,
                    "title" to entity.title,
                    "durationMinutes" to entity.durationMinutes,
                    "completedSeconds" to entity.completedSeconds,
                    "isCompleted" to entity.isCompleted,
                    "isCurrentFocus" to entity.isCurrentFocus,
                    "createdAtEpochMs" to entity.createdAtEpochMs,
                    "updatedAtEpochMs" to entity.updatedAtEpochMs,
                )
                firestore.collection("users")
                    .document(uid)
                    .collection("tasks")
                    .document(entity.id)
                    .set(data, SetOptions.merge())
                    .await()
            }
        }
    }

    private fun deleteTaskFromFirestore(taskId: String) {
        runCatching {
            syncRepositoryProvider.get().enqueueSync()
        }
        val uid = authRepository.currentUser.value?.uid ?: return
        scope.launch {
            runCatching {
                firestore.collection("users")
                    .document(uid)
                    .collection("tasks")
                    .document(taskId)
                    .delete()
                    .await()
            }
        }
    }
}
