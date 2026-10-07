package com.focusflow.core.domain.repository

import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.SessionMode
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for the focus session.
 *
 * Rules from AGENTS.md:
 * - "A single source of truth per datum. The focus session lives in one repository shared by the UI and the service."
 * - "Local storage is the source of truth for anything visible or that affects blocking."
 */
interface FocusSessionRepository {

    /**
     * Emits the current active session, or null if no session is running/paused.
     */
    val currentSession: Flow<FocusSession?>

    /**
     * Starts a new session and persists it immediately.
     */
    suspend fun startSession(
        mode: SessionMode,
        tagId: String? = null,
        customTargetDurationMs: Long? = null,
    ): FocusSession

    /**
     * Pauses the currently running session.
     */
    suspend fun pauseSession(): FocusSession?

    /**
     * Resumes the currently paused session.
     */
    suspend fun resumeSession(): FocusSession?

    /**
     * Concludes the session as successfully completed.
     */
    suspend fun completeSession(): FocusSession?

    /**
     * Aborts the session early.
     */
    suspend fun abortSession(): FocusSession?

    /**
     * Returns historical sessions ordered by start time descending.
     */
    fun getSessionHistory(limit: Int = 50): Flow<List<FocusSession>>
}
