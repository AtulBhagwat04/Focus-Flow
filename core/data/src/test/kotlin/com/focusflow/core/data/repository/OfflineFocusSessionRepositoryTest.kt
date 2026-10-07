package com.focusflow.core.data.repository

import com.focusflow.core.common.time.Clock
import com.focusflow.core.data.local.dao.FocusSessionDao
import com.focusflow.core.data.local.entity.FocusSessionEntity
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.core.domain.model.SessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class OfflineFocusSessionRepositoryTest {

    private class FakeFocusSessionDao : FocusSessionDao {
        private val sessions = MutableStateFlow<Map<String, FocusSessionEntity>>(emptyMap())

        override fun getActiveSession(): Flow<FocusSessionEntity?> =
            sessions.map { map ->
                map.values.firstOrNull { it.state in listOf("RUNNING", "PAUSED") }
            }

        override fun getHistoricalSessions(limit: Int): Flow<List<FocusSessionEntity>> =
            sessions.map { map ->
                map.values
                    .filter { it.state in listOf("COMPLETED", "ABORTED") }
                    .sortedByDescending { it.startedAtEpochMs }
                    .take(limit)
            }

        override suspend fun upsertSession(session: FocusSessionEntity) {
            sessions.value = sessions.value + (session.id to session)
        }

        override suspend fun getSessionById(id: String): FocusSessionEntity? =
            sessions.value[id]

        override suspend fun deleteSession(id: String) {
            sessions.value = sessions.value - id
        }
    }

    private class FakeClock(var currentTimeMs: Long) : Clock {
        override fun now(): Long = currentTimeMs
    }

    private lateinit var dao: FakeFocusSessionDao
    private lateinit var clock: FakeClock
    private lateinit var repository: OfflineFocusSessionRepository

    @Before
    fun setup() {
        dao = FakeFocusSessionDao()
        clock = FakeClock(1_000_000L)
        repository = OfflineFocusSessionRepository(dao, clock)
    }

    @Test
    fun startSession_emitsRunningSession() = runBlocking {
        val started = repository.startSession(
            mode = SessionMode.Pomodoro(),
            tagId = "tag_coding",
        )

        assertEquals(SessionState.RUNNING, started.state)
        assertEquals(1_000_000L, started.startedAtEpochMs)

        val active = repository.currentSession.first()
        assertNotNull(active)
        assertEquals(started.id, active?.id)
        assertEquals(SessionState.RUNNING, active?.state)
    }

    @Test
    fun pauseAndResume_updatesAccumulatedPauseDuration() = runBlocking {
        repository.startSession(SessionMode.Pomodoro())

        // Forward 5 minutes, pause
        clock.currentTimeMs += 300_000L
        val paused = repository.pauseSession()
        assertNotNull(paused)
        assertEquals(SessionState.PAUSED, paused?.state)

        // Forward 2 minutes while paused, resume
        clock.currentTimeMs += 120_000L
        val resumed = repository.resumeSession()
        assertNotNull(resumed)
        assertEquals(SessionState.RUNNING, resumed?.state)
        assertEquals(120_000L, resumed?.totalPausedDurationMs)

        // Net focus duration after total 7 minutes clock time is 5 minutes
        val active = repository.currentSession.first()
        assertEquals(300_000L, active?.elapsedDurationMs(clock.currentTimeMs))
    }

    @Test
    fun completeSession_transitionsToCompletedWithXp() = runBlocking {
        repository.startSession(SessionMode.Pomodoro())

        // 25 minutes elapsed
        clock.currentTimeMs += 25 * 60 * 1000L
        val completed = repository.completeSession()

        assertNotNull(completed)
        assertEquals(SessionState.COMPLETED, completed?.state)
        assertEquals(25, completed?.xpEarned)

        // No active session remaining
        val active = repository.currentSession.first()
        assertNull(active)

        // Historical sessions contains completed session
        val history = repository.getSessionHistory(10).first()
        assertEquals(1, history.size)
        assertEquals(completed?.id, history.first().id)
    }

    @Test
    fun processDeathRecovery_restoresSessionStateFromDao() = runBlocking {
        // Start session in initial process
        val originalSession = repository.startSession(SessionMode.Pomodoro())
        clock.currentTimeMs += 600_000L // 10 minutes pass

        // Simulate process death and new repository instance connecting to same DAO
        val resurrectedRepository = OfflineFocusSessionRepository(dao, clock)
        val restored = resurrectedRepository.currentSession.first()

        assertNotNull(restored)
        assertEquals(originalSession.id, restored?.id)
        assertEquals(SessionState.RUNNING, restored?.state)
        // Correctly calculates 10 minutes elapsed without needing an ongoing background tick loop
        assertEquals(600_000L, restored?.elapsedDurationMs(clock.currentTimeMs))
    }
}
