package com.focusflow.core.common.time

import javax.inject.Inject

/**
 * Injected clock abstraction — NEVER call System.currentTimeMillis() directly in app code.
 *
 * Rule from STATE_MANAGEMENT: "Time is stored as timestamps. Inject a clock."
 * This makes all time-sensitive code testable with a fake clock.
 */
interface Clock {
    /** Returns current time as epoch milliseconds. */
    fun now(): Long
}

/**
 * Production implementation that delegates to the system clock.
 * Injected as a singleton via Hilt — see di/CoreModule.kt.
 */
class SystemClock @Inject constructor() : Clock {
    override fun now(): Long = System.currentTimeMillis()
}
