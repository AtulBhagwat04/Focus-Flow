package com.focusflow.core.data.repository

import com.focusflow.core.common.time.Clock
import com.focusflow.core.data.local.dao.FocusSessionDao
import com.focusflow.core.data.local.entity.asDomainModel
import com.focusflow.core.data.local.entity.asEntity
import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.core.domain.model.SessionState
import com.focusflow.core.domain.repository.FocusSessionRepository
import com.focusflow.core.domain.statemachine.FocusStateMachine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFocusSessionRepository @Inject constructor(
    private val focusSessionDao: FocusSessionDao,
    private val clock: Clock,
) : FocusSessionRepository {

    override val currentSession: Flow<FocusSession?> =
        focusSessionDao.getActiveSession().map { it?.asDomainModel() }

    override suspend fun startSession(
        mode: SessionMode,
        tagId: String?,
        customTargetDurationMs: Long?,
    ): FocusSession {
        val newSession = FocusStateMachine.start(
            id = UUID.randomUUID().toString(),
            mode = mode,
            tagId = tagId,
            startTimeEpochMs = clock.now(),
            customTargetDurationMs = customTargetDurationMs,
        )
        focusSessionDao.upsertSession(newSession.asEntity())
        return newSession
    }

    override suspend fun pauseSession(): FocusSession? {
        val current = focusSessionDao.getActiveSession().firstOrNull()?.asDomainModel()
        return if (current == null || current.state != SessionState.RUNNING) {
            null
        } else {
            val paused = FocusStateMachine.pause(current, clock.now())
            focusSessionDao.upsertSession(paused.asEntity())
            paused
        }
    }

    override suspend fun resumeSession(): FocusSession? {
        val current = focusSessionDao.getActiveSession().firstOrNull()?.asDomainModel()
        return if (current == null || current.state != SessionState.PAUSED) {
            null
        } else {
            val resumed = FocusStateMachine.resume(current, clock.now())
            focusSessionDao.upsertSession(resumed.asEntity())
            resumed
        }
    }

    override suspend fun completeSession(): FocusSession? {
        val current = focusSessionDao.getActiveSession().firstOrNull()?.asDomainModel()
        return if (current == null || !current.state.isActive) {
            null
        } else {
            val completed = FocusStateMachine.complete(current, clock.now())
            focusSessionDao.upsertSession(completed.asEntity())
            completed
        }
    }

    override suspend fun abortSession(): FocusSession? {
        val current = focusSessionDao.getActiveSession().firstOrNull()?.asDomainModel()
        return if (current == null || !current.state.isActive) {
            null
        } else {
            val aborted = FocusStateMachine.abort(current, clock.now())
            focusSessionDao.upsertSession(aborted.asEntity())
            aborted
        }
    }

    override fun getSessionHistory(limit: Int): Flow<List<FocusSession>> =
        focusSessionDao.getHistoricalSessions(limit).map { list ->
            list.map { it.asDomainModel() }
        }
}
