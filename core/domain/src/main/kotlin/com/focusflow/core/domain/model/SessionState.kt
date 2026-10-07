package com.focusflow.core.domain.model

/**
 * Lifecycle states of a focus session.
 *
 * Transitions:
 * IDLE -> RUNNING
 * RUNNING -> PAUSED
 * RUNNING -> COMPLETED
 * RUNNING -> ABORTED
 * PAUSED -> RUNNING
 * PAUSED -> ABORTED
 */
enum class SessionState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED,
    ABORTED;

    val isActive: Boolean
        get() = this == RUNNING || this == PAUSED

    val isTerminal: Boolean
        get() = this == COMPLETED || this == ABORTED
}
