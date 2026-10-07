package com.focusflow.core.domain.statemachine

import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.core.domain.model.SessionState

/**
 * Pure state machine governing focus session transitions.
 *
 * Rules from AGENTS.md:
 * - "The domain layer is pure Kotlin with no Android or Firebase classes."
 * - "Time is stored as timestamps, never as tick counts."
 * - "Persist every focus-session transition so it can be rebuilt after process death or reboot."
 */
object FocusStateMachine {

    const val MILLIS_PER_MINUTE = 60_000L
    const val MIN_MINUTES_FOR_XP = 5L

    /**
     * Initializes and starts a new focus session in RUNNING state.
     */
    fun start(
        id: String,
        mode: SessionMode,
        tagId: String? = null,
        startTimeEpochMs: Long,
        customTargetDurationMs: Long? = null,
    ): FocusSession {
        val targetDurationMs = customTargetDurationMs ?: when (mode) {
            is SessionMode.Pomodoro -> mode.workDurationMs
            is SessionMode.Custom -> mode.durationMs
            is SessionMode.UntilTime -> (mode.targetEpochMs - startTimeEpochMs).coerceAtLeast(0L)
            is SessionMode.Stopwatch -> 0L
        }

        return FocusSession(
            id = id,
            mode = mode,
            state = SessionState.RUNNING,
            tagId = tagId,
            targetDurationMs = targetDurationMs,
            startedAtEpochMs = startTimeEpochMs,
            pausedAtEpochMs = null,
            totalPausedDurationMs = 0L,
            completedAtEpochMs = null,
            xpEarned = 0,
        )
    }

    /**
     * Transitions an active session from RUNNING to PAUSED.
     *
     * @throws IllegalStateException if session is not currently [SessionState.RUNNING].
     */
    fun pause(session: FocusSession, pauseTimeEpochMs: Long): FocusSession {
        check(session.state == SessionState.RUNNING) {
            "Cannot pause session in state ${session.state}. Session must be RUNNING."
        }
        return session.copy(
            state = SessionState.PAUSED,
            pausedAtEpochMs = pauseTimeEpochMs,
        )
    }

    /**
     * Transitions an active session from PAUSED to RUNNING.
     * Accumulates the paused interval into [FocusSession.totalPausedDurationMs].
     *
     * @throws IllegalStateException if session is not currently [SessionState.PAUSED].
     */
    fun resume(session: FocusSession, resumeTimeEpochMs: Long): FocusSession {
        check(session.state == SessionState.PAUSED) {
            "Cannot resume session in state ${session.state}. Session must be PAUSED."
        }
        val lastPausedAt = session.pausedAtEpochMs ?: resumeTimeEpochMs
        val pauseDeltaMs = (resumeTimeEpochMs - lastPausedAt).coerceAtLeast(0L)

        return session.copy(
            state = SessionState.RUNNING,
            pausedAtEpochMs = null,
            totalPausedDurationMs = session.totalPausedDurationMs + pauseDeltaMs,
        )
    }

    /**
     * Concludes a session successfully as COMPLETED.
     *
     * @throws IllegalStateException if session is already terminal.
     */
    fun complete(
        session: FocusSession,
        completeTimeEpochMs: Long,
        xpEarned: Int? = null,
    ): FocusSession {
        check(session.state.isActive) {
            "Cannot complete session in terminal state ${session.state}."
        }

        var accumulatedPause = session.totalPausedDurationMs
        if (session.state == SessionState.PAUSED && session.pausedAtEpochMs != null) {
            accumulatedPause += (completeTimeEpochMs - session.pausedAtEpochMs).coerceAtLeast(0L)
        }

        val completedSession = session.copy(
            state = SessionState.COMPLETED,
            pausedAtEpochMs = null,
            totalPausedDurationMs = accumulatedPause,
            completedAtEpochMs = completeTimeEpochMs,
        )

        val xp = xpEarned ?: calculateBaseXp(completedSession, completeTimeEpochMs)
        return completedSession.copy(xpEarned = xp)
    }

    /**
     * Aborts an active session before its target is reached.
     *
     * @throws IllegalStateException if session is already terminal.
     */
    fun abort(session: FocusSession, abortTimeEpochMs: Long): FocusSession {
        check(session.state.isActive) {
            "Cannot abort session in terminal state ${session.state}."
        }

        var accumulatedPause = session.totalPausedDurationMs
        if (session.state == SessionState.PAUSED && session.pausedAtEpochMs != null) {
            accumulatedPause += (abortTimeEpochMs - session.pausedAtEpochMs).coerceAtLeast(0L)
        }

        return session.copy(
            state = SessionState.ABORTED,
            pausedAtEpochMs = null,
            totalPausedDurationMs = accumulatedPause,
            completedAtEpochMs = abortTimeEpochMs,
            xpEarned = 0,
        )
    }

    /**
     * Calculates base XP earned for a completed session.
     * Awards 1 XP per full minute of net focused time (minimum 5 minutes required).
     */
    fun calculateBaseXp(session: FocusSession, currentEpochMs: Long): Int {
        val elapsedMs = session.elapsedDurationMs(currentEpochMs)
        val elapsedMinutes = elapsedMs / MILLIS_PER_MINUTE
        return if (elapsedMinutes >= MIN_MINUTES_FOR_XP) elapsedMinutes.toInt() else 0
    }
}
