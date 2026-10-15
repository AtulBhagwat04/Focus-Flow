package com.focusflow.core.domain.blocking

import com.focusflow.core.domain.blocking.engine.BlockingRulesEngine
import com.focusflow.core.domain.blocking.engine.BlockingSignals
import com.focusflow.core.domain.blocking.model.AppBlockRule
import com.focusflow.core.domain.blocking.model.BlockReason
import com.focusflow.core.domain.blocking.model.BlockingDecision
import com.focusflow.core.domain.blocking.model.EmergencyPass
import com.focusflow.core.domain.model.SessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockingRulesEngineTest {

    private val testPackage = "com.example.distractingapp"
    private val nowMs = 1_000_000L

    @Test
    fun `safe listed packages are always allowed`() {
        val signals = BlockingSignals(
            packageName = "com.android.dialer",
            nowEpochMs = nowMs,
            dayOfWeek = 1,
            minuteOfDay = 600,
            sessionState = SessionState.RUNNING,
            isDefaultDialer = true,
        )

        val decision = BlockingRulesEngine.evaluate(signals)
        assertEquals(BlockingDecision.Allow, decision)
    }

    @Test
    fun `user essential packages are always allowed`() {
        val signals = BlockingSignals(
            packageName = testPackage,
            nowEpochMs = nowMs,
            dayOfWeek = 1,
            minuteOfDay = 600,
            sessionState = SessionState.RUNNING,
            userEssentials = setOf(testPackage),
        )

        val decision = BlockingRulesEngine.evaluate(signals)
        assertEquals(BlockingDecision.Allow, decision)
    }

    @Test
    fun `active focus session blocks unconfigured apps by default`() {
        val signals = BlockingSignals(
            packageName = testPackage,
            nowEpochMs = nowMs,
            dayOfWeek = 1,
            minuteOfDay = 600,
            sessionState = SessionState.RUNNING,
            remainingSessionDurationMs = 1500L,
            blockRule = null, // No explicit rule saved
        )

        val decision = BlockingRulesEngine.evaluate(signals)
        assertTrue(decision is BlockingDecision.Block)
        val block = decision as BlockingDecision.Block
        assertEquals(BlockReason.IN_FOCUS_SESSION, block.reason)
    }

    @Test
    fun `active focus session allows apps explicitly unblocked during focus`() {
        val signals = BlockingSignals(
            packageName = testPackage,
            nowEpochMs = nowMs,
            dayOfWeek = 1,
            minuteOfDay = 600,
            sessionState = SessionState.RUNNING,
            blockRule = AppBlockRule(
                packageName = testPackage,
                blockDuringFocus = false,
            ),
        )

        val decision = BlockingRulesEngine.evaluate(signals)
        assertEquals(BlockingDecision.Allow, decision)
    }

    @Test
    fun `emergency pass bypasses focus session blocking`() {
        val signals = BlockingSignals(
            packageName = testPackage,
            nowEpochMs = nowMs,
            dayOfWeek = 1,
            minuteOfDay = 600,
            sessionState = SessionState.RUNNING,
            emergencyPass = EmergencyPass(
                expiresAtEpochMs = nowMs + 300_000L,
            ),
        )

        val decision = BlockingRulesEngine.evaluate(signals)
        assertEquals(BlockingDecision.Allow, decision)
    }

    @Test
    fun `idle session does not block normal unconfigured app`() {
        val signals = BlockingSignals(
            packageName = testPackage,
            nowEpochMs = nowMs,
            dayOfWeek = 1,
            minuteOfDay = 600,
            sessionState = SessionState.IDLE,
            blockRule = null,
        )

        val decision = BlockingRulesEngine.evaluate(signals)
        assertEquals(BlockingDecision.Allow, decision)
    }
}
