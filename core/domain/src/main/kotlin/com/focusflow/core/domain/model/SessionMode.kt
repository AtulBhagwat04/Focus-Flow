package com.focusflow.core.domain.model

/**
 * Operating modes for a focus timer session.
 *
 * Per PROJECT_BRIEF.md §3.2:
 * "Modes: Pomodoro, custom focus/break, stopwatch, 'until a chosen time'."
 */
sealed interface SessionMode {

    /**
     * Standard Pomodoro technique mode with work and break intervals.
     */
    data class Pomodoro(
        val workDurationMs: Long = DEFAULT_WORK_DURATION_MS,
        val breakDurationMs: Long = DEFAULT_BREAK_DURATION_MS,
    ) : SessionMode {
        companion object {
            const val DEFAULT_WORK_DURATION_MS = 25 * 60 * 1000L  // 25 minutes
            const val DEFAULT_BREAK_DURATION_MS = 5 * 60 * 1000L   // 5 minutes
        }
    }

    /**
     * Open-ended count-up stopwatch mode.
     */
    data object Stopwatch : SessionMode

    /**
     * Custom fixed-duration countdown mode.
     */
    data class Custom(
        val durationMs: Long,
    ) : SessionMode

    /**
     * Focus until a specific target timestamp is reached.
     */
    data class UntilTime(
        val targetEpochMs: Long,
    ) : SessionMode
}
