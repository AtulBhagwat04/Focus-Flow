package com.focusflow.core.domain.statemachine

import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.core.domain.model.SessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FocusStateMachineTest {

    private val startTime = 1_000_000L

    @Test
    fun start_pomodoroMode_initializesRunningSession() {
        val session = FocusStateMachine.start(
            id = "test-1",
            mode = SessionMode.Pomodoro(),
            tagId = "tag-coding",
            startTimeEpochMs = startTime,
        )

        assertEquals("test-1", session.id)
        assertEquals(SessionState.RUNNING, session.state)
        assertEquals(25 * 60 * 1000L, session.targetDurationMs)
        assertEquals(startTime, session.startedAtEpochMs)
        assertNull(session.pausedAtEpochMs)
        assertEquals(0L, session.totalPausedDurationMs)
        assertEquals("tag-coding", session.tagId)
        assertFalse(session.isTargetReached(startTime))
    }

    @Test
    fun start_stopwatchMode_targetDurationIsZero() {
        val session = FocusStateMachine.start(
            id = "test-2",
            mode = SessionMode.Stopwatch,
            startTimeEpochMs = startTime,
        )

        assertEquals(0L, session.targetDurationMs)
        assertEquals(0L, session.remainingDurationMs(startTime + 60_000L))
        assertEquals(0f, session.progressFraction(startTime + 60_000L), 0.001f)
        assertEquals(60_000L, session.elapsedDurationMs(startTime + 60_000L))
    }

    @Test
    fun pause_fromRunning_setsPausedStateAndTimestamp() {
        val session = FocusStateMachine.start("test", SessionMode.Pomodoro(), startTimeEpochMs = startTime)
        val pauseTime = startTime + 300_000L // 5 mins later

        val paused = FocusStateMachine.pause(session, pauseTime)

        assertEquals(SessionState.PAUSED, paused.state)
        assertEquals(pauseTime, paused.pausedAtEpochMs)
        assertEquals(0L, paused.totalPausedDurationMs)
    }

    @Test(expected = IllegalStateException::class)
    fun pause_whenAlreadyPaused_throwsIllegalStateException() {
        val session = FocusStateMachine.start("test", SessionMode.Pomodoro(), startTimeEpochMs = startTime)
        val paused = FocusStateMachine.pause(session, startTime + 10_000L)

        FocusStateMachine.pause(paused, startTime + 20_000L)
    }

    @Test
    fun resume_fromPaused_accumulatesPauseDurationAndResetsPausedTimestamp() {
        val session = FocusStateMachine.start("test", SessionMode.Pomodoro(), startTimeEpochMs = startTime)
        val pauseTime = startTime + 300_000L
        val paused = FocusStateMachine.pause(session, pauseTime)

        val resumeTime = pauseTime + 60_000L // Paused for 1 minute
        val resumed = FocusStateMachine.resume(paused, resumeTime)

        assertEquals(SessionState.RUNNING, resumed.state)
        assertNull(resumed.pausedAtEpochMs)
        assertEquals(60_000L, resumed.totalPausedDurationMs)

        // Elapsed time at resume time should be exactly 5 minutes (300s), not 6 minutes
        assertEquals(300_000L, resumed.elapsedDurationMs(resumeTime))
    }

    @Test(expected = IllegalStateException::class)
    fun resume_whenAlreadyRunning_throwsIllegalStateException() {
        val session = FocusStateMachine.start("test", SessionMode.Pomodoro(), startTimeEpochMs = startTime)
        FocusStateMachine.resume(session, startTime + 10_000L)
    }

    @Test
    fun multiplePauses_correctlyAccumulateTotalPausedDuration() {
        var session = FocusStateMachine.start("test", SessionMode.Pomodoro(), startTimeEpochMs = startTime)

        // First pause: 2 minutes
        session = FocusStateMachine.pause(session, startTime + 120_000L)
        session = FocusStateMachine.resume(session, startTime + 240_000L)
        assertEquals(120_000L, session.totalPausedDurationMs)

        // Second pause: 3 minutes
        session = FocusStateMachine.pause(session, startTime + 360_000L)
        session = FocusStateMachine.resume(session, startTime + 540_000L)
        assertEquals(300_000L, session.totalPausedDurationMs) // 2m + 3m = 5m

        // Total clock elapsed is 600s, net elapsed focus is 600s - 300s = 300s
        assertEquals(300_000L, session.elapsedDurationMs(startTime + 600_000L))
    }

    @Test
    fun complete_fromRunning_setsCompletedStateAndCalculatesXp() {
        val session = FocusStateMachine.start("test", SessionMode.Pomodoro(), startTimeEpochMs = startTime)
        val completeTime = startTime + (25 * 60 * 1000L)

        val completed = FocusStateMachine.complete(session, completeTime)

        assertEquals(SessionState.COMPLETED, completed.state)
        assertEquals(completeTime, completed.completedAtEpochMs)
        assertEquals(25, completed.xpEarned) // 25 mins = 25 XP
        assertTrue(completed.isTargetReached(completeTime))
    }

    @Test
    fun complete_fromPaused_accountsForFinalPauseDuration() {
        val session = FocusStateMachine.start("test", SessionMode.Pomodoro(), startTimeEpochMs = startTime)
        val paused = FocusStateMachine.pause(session, startTime + 600_000L) // 10m in
        val completeTime = startTime + 900_000L // paused for 5m, then ended

        val completed = FocusStateMachine.complete(paused, completeTime)

        assertEquals(SessionState.COMPLETED, completed.state)
        assertEquals(300_000L, completed.totalPausedDurationMs)
        assertEquals(600_000L, completed.elapsedDurationMs(completeTime))
        assertEquals(10, completed.xpEarned) // 10 minutes focused = 10 XP
    }

    @Test
    fun abort_fromRunning_setsAbortedStateWithZeroXp() {
        val session = FocusStateMachine.start("test", SessionMode.Pomodoro(), startTimeEpochMs = startTime)
        val abortTime = startTime + 600_000L

        val aborted = FocusStateMachine.abort(session, abortTime)

        assertEquals(SessionState.ABORTED, aborted.state)
        assertEquals(abortTime, aborted.completedAtEpochMs)
        assertEquals(0, aborted.xpEarned)
        assertFalse(aborted.state.isActive)
        assertTrue(aborted.state.isTerminal)
    }

    @Test
    fun remainingDuration_and_progressFraction_behaveCorrectly() {
        val targetMs = 1_000_000L
        val session = FocusStateMachine.start(
            id = "test",
            mode = SessionMode.Custom(targetMs),
            startTimeEpochMs = startTime,
        )

        // 25% elapsed
        val quarterTime = startTime + 250_000L
        assertEquals(750_000L, session.remainingDurationMs(quarterTime))
        assertEquals(0.25f, session.progressFraction(quarterTime), 0.001f)
        assertFalse(session.isTargetReached(quarterTime))

        // 100% elapsed
        val fullTime = startTime + 1_000_000L
        assertEquals(0L, session.remainingDurationMs(fullTime))
        assertEquals(1.0f, session.progressFraction(fullTime), 0.001f)
        assertTrue(session.isTargetReached(fullTime))

        // Over target (clamp remaining to 0, fraction to 1.0)
        val overTime = startTime + 1_200_000L
        assertEquals(0L, session.remainingDurationMs(overTime))
        assertEquals(1.0f, session.progressFraction(overTime), 0.001f)
        assertTrue(session.isTargetReached(overTime))
    }
}
