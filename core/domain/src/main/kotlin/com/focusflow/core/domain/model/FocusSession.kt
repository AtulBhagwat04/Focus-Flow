package com.focusflow.core.domain.model

/**
 * Pure domain representation of a focus session.
 *
 * Rules from AGENTS.md:
 * - "Time is stored as timestamps, never as tick counts."
 * - "Persist every focus-session transition so it can be rebuilt after process death or reboot."
 */
data class FocusSession(
    val id: String,
    val mode: SessionMode,
    val state: SessionState,
    val tagId: String? = null,
    val targetDurationMs: Long = 0L,
    val startedAtEpochMs: Long,
    val pausedAtEpochMs: Long? = null,
    val totalPausedDurationMs: Long = 0L,
    val completedAtEpochMs: Long? = null,
    val xpEarned: Int = 0,
) {

    /**
     * Calculates the net elapsed focus time in milliseconds up to [currentEpochMs],
     * excluding any time spent in paused state.
     */
    fun elapsedDurationMs(currentEpochMs: Long): Long {
        val effectiveEndMs = when (state) {
            SessionState.COMPLETED, SessionState.ABORTED -> completedAtEpochMs ?: currentEpochMs
            SessionState.PAUSED -> pausedAtEpochMs ?: currentEpochMs
            SessionState.RUNNING -> currentEpochMs
            SessionState.IDLE -> startedAtEpochMs
        }
        val grossDuration = effectiveEndMs - startedAtEpochMs
        return (grossDuration - totalPausedDurationMs).coerceAtLeast(0L)
    }

    /**
     * Calculates the remaining focus time for countdown modes.
     * Returns 0 for Stopwatch mode or when the target duration is reached.
     */
    fun remainingDurationMs(currentEpochMs: Long): Long {
        if (mode is SessionMode.Stopwatch) return 0L
        val elapsed = elapsedDurationMs(currentEpochMs)
        return (targetDurationMs - elapsed).coerceAtLeast(0L)
    }

    /**
     * Normalized progress value between 0.0f and 1.0f.
     */
    fun progressFraction(currentEpochMs: Long): Float {
        if (targetDurationMs <= 0L || mode is SessionMode.Stopwatch) return 0f
        val elapsed = elapsedDurationMs(currentEpochMs)
        return (elapsed.toFloat() / targetDurationMs.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Indicates whether the target duration has been reached.
     */
    fun isTargetReached(currentEpochMs: Long): Boolean {
        if (mode is SessionMode.Stopwatch || targetDurationMs <= 0L) return false
        return elapsedDurationMs(currentEpochMs) >= targetDurationMs
    }
}
