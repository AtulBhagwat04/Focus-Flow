package com.focusflow.core.common.time

import org.junit.Assert.assertTrue
import org.junit.Test

class ClockTest {

    @Test
    fun systemClock_returnsPositiveEpochMillis() {
        val clock = SystemClock()
        val before = System.currentTimeMillis()
        val now = clock.now()
        val after = System.currentTimeMillis()

        assertTrue(now >= before)
        assertTrue(now <= after)
    }
}
