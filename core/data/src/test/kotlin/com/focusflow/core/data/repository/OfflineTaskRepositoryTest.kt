package com.focusflow.core.data.repository

import com.focusflow.core.data.local.dao.TaskDao
import com.focusflow.core.data.local.entity.TaskEntity
import com.focusflow.core.domain.auth.model.UserAccount
import com.focusflow.core.domain.auth.model.UserProfile
import com.focusflow.core.domain.auth.repository.AuthRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OfflineTaskRepositoryTest {

    private class FakeTaskDao : TaskDao {
        private val tasksMap = MutableStateFlow<Map<String, TaskEntity>>(emptyMap())

        override fun getTasks(): Flow<List<TaskEntity>> = tasksMap.map { it.values.toList() }
        override fun getActiveTask(): Flow<TaskEntity?> =
            tasksMap.map { it.values.firstOrNull { t -> t.isCurrentFocus } }

        override suspend fun getTaskById(id: String): TaskEntity? = tasksMap.value[id]
        override suspend fun getAllTasks(): List<TaskEntity> = tasksMap.value.values.toList()

        override suspend fun upsertTask(task: TaskEntity) {
            tasksMap.value = tasksMap.value + (task.id to task)
        }

        override suspend fun upsertTasks(tasks: List<TaskEntity>) {
            val updated = tasksMap.value.toMutableMap()
            tasks.forEach { updated[it.id] = it }
            tasksMap.value = updated
        }

        override suspend fun clearActiveTask() {
            tasksMap.value = tasksMap.value.mapValues { it.value.copy(isCurrentFocus = false) }
        }

        override suspend fun setActiveTask(id: String) {
            tasksMap.value = tasksMap.value.mapValues { it.value.copy(isCurrentFocus = it.key == id) }
        }

        override suspend fun deleteTask(taskId: String) {
            tasksMap.value = tasksMap.value - taskId
        }

        override suspend fun deleteAllTasks() {
            tasksMap.value = emptyMap()
        }
    }

    private class FakeAuthRepository(initialUser: UserAccount? = null) : AuthRepository {
        val userFlow = MutableStateFlow(initialUser)
        override val currentUser: StateFlow<UserAccount?> = userFlow
        override val userProfile: Flow<UserProfile?> = flowOf(null)
        override suspend fun signInAnonymously(): Result<UserAccount> = Result.success(UserAccount("test-uid"))
        override suspend fun signInWithGoogle(idToken: String): Result<UserAccount> = Result.success(UserAccount("test-uid"))
        override suspend fun linkGoogleAccount(idToken: String): Result<UserAccount> = Result.success(UserAccount("test-uid"))
        override suspend fun signOut(): Result<Unit> = Result.success(Unit)
        override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
    }

    private lateinit var dao: FakeTaskDao
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var firestore: FirebaseFirestore
    private lateinit var tasksCollection: CollectionReference
    private lateinit var taskDoc: DocumentReference
    private lateinit var syncRepository: com.focusflow.core.domain.sync.repository.SyncRepository
    private lateinit var repository: OfflineTaskRepository

    @Before
    fun setUp() {
        dao = FakeTaskDao()
        authRepository = FakeAuthRepository(UserAccount(uid = "user-123"))

        firestore = mockk(relaxed = true)
        val usersCollection = mockk<CollectionReference>(relaxed = true)
        val userDoc = mockk<DocumentReference>(relaxed = true)
        tasksCollection = mockk(relaxed = true)
        taskDoc = mockk(relaxed = true)
        syncRepository = mockk(relaxed = true)

        every { firestore.collection("users") } returns usersCollection
        every { usersCollection.document("user-123") } returns userDoc
        every { userDoc.collection("tasks") } returns tasksCollection
        every { tasksCollection.document(any()) } returns taskDoc
        every { taskDoc.set(any(), any<SetOptions>()) } returns Tasks.forResult(null)
        every { taskDoc.delete() } returns Tasks.forResult(null)

        repository = OfflineTaskRepository(
            taskDao = dao,
            firestore = firestore,
            authRepository = authRepository,
            syncRepositoryProvider = { syncRepository },
        )
    }

    @Test
    fun addTask_persistsToDao_andSavesToFirestore() = runBlocking {
        val task = repository.addTask(title = "Study Kotlin", durationMinutes = 30)

        assertNotNull(task.id)
        assertEquals("Study Kotlin", task.title)
        assertEquals(30, task.durationMinutes)
        assertEquals(0, task.completedSeconds)
        assertFalse(task.isCompleted)

        // Verify persisted in DAO
        val daoTask = dao.getTaskById(task.id)
        assertNotNull(daoTask)
        assertEquals("Study Kotlin", daoTask?.title)

        // Verify Firestore was called with task data
        val slot = io.mockk.slot<Map<String, Any?>>()
        verify { tasksCollection.document(task.id) }
        verify { taskDoc.set(capture(slot), any<SetOptions>()) }
        assertEquals("Study Kotlin", slot.captured["title"])
    }

    @Test
    fun toggleTaskCompleted_updatesDao_andSyncsToFirestore() = runBlocking {
        val task = repository.addTask(title = "Task to toggle", durationMinutes = 20)

        val updated = repository.toggleTaskCompleted(task.id)
        assertNotNull(updated)
        assertTrue(updated!!.isCompleted)
        assertEquals(20 * 60, updated.completedSeconds)

        val daoTask = dao.getTaskById(task.id)
        assertTrue(daoTask!!.isCompleted)

        // Toggle back
        val reverted = repository.toggleTaskCompleted(task.id)
        assertNotNull(reverted)
        assertFalse(reverted!!.isCompleted)
        assertEquals(0, reverted.completedSeconds)
    }

    @Test
    fun selectTaskForFocus_setsActive_andSyncsToFirestore() = runBlocking {
        val task = repository.addTask(title = "Deep work task", durationMinutes = 45)

        val selected = repository.selectTaskForFocus(task.id)
        assertNotNull(selected)
        assertTrue(selected!!.isCurrentFocus)

        val active = repository.activeTask.first()
        assertNotNull(active)
        assertEquals(task.id, active?.id)

        repository.clearActiveTask()
        val cleared = repository.activeTask.first()
        assertNull(cleared)
    }

    @Test
    fun recordFocusProgress_accumulatesSeconds_andCompletesWhenTargetReached() = runBlocking {
        val task = repository.addTask(title = "Progress task", durationMinutes = 10) // 600s

        val progress1 = repository.recordFocusProgress(task.id, 300)
        assertNotNull(progress1)
        assertEquals(300, progress1!!.completedSeconds)
        assertFalse(progress1.isCompleted)

        val progress2 = repository.recordFocusProgress(task.id, 350)
        assertNotNull(progress2)
        assertEquals(600, progress2!!.completedSeconds) // Coerced to max
        assertTrue(progress2.isCompleted)
    }

    @Test
    fun completeTask_marksComplete_andClearsActiveIfFocused() = runBlocking {
        val task = repository.addTask(title = "To complete", durationMinutes = 15)
        repository.selectTaskForFocus(task.id)

        val completed = repository.completeTask(task.id)
        assertNotNull(completed)
        assertTrue(completed!!.isCompleted)
        assertEquals(15 * 60, completed.completedSeconds)
        assertFalse(completed.isCurrentFocus)
    }

    @Test
    fun deleteTask_removesFromDao_andDeletesFromFirestore() = runBlocking {
        val task = repository.addTask(title = "To delete", durationMinutes = 10)
        assertNotNull(dao.getTaskById(task.id))

        repository.deleteTask(task.id)
        assertNull(dao.getTaskById(task.id))
        verify { taskDoc.delete() }
    }

    @Test
    fun offline_unauthenticated_stillSucceedsLocally() = runBlocking {
        val unauthRepo = FakeAuthRepository(initialUser = null)
        val offlineRepository = OfflineTaskRepository(
            taskDao = dao,
            firestore = firestore,
            authRepository = unauthRepo,
            syncRepositoryProvider = { syncRepository },
        )

        val task = offlineRepository.addTask(title = "Offline task", durationMinutes = 25)
        assertNotNull(task)
        assertEquals("Offline task", task.title)

        val daoTask = dao.getTaskById(task.id)
        assertNotNull(daoTask)
        assertEquals("Offline task", daoTask?.title)

        // Verify WorkManager was asked to enqueue sync for when network connects
        verify { syncRepository.enqueueSync() }
    }
}
