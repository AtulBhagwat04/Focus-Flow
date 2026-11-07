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

    // ── Selective Mode (isStrictFocusMode = false, the default) ──────────────

    @Test
    fun `selective mode - app with blockDuringFocus=true is blocked during session`() {
        val signals = BlockingSignals(
            packageName = testPackage,
            nowEpochMs = nowMs,
            dayOfWeek = 1,
            minuteOfDay = 600,
            sessionState = SessionState.RUNNING,
            remainingSessionDurationMs = 1500L,
            blockRule = AppBlockRule(packageName = testPackage, blockDuringFocus = true),
            isStrictFocusMode = false,
        )

        val decision = BlockingRulesEngine.evaluate(signals)
        assertTrue(decision is BlockingDecision.Block)
        val block = decision as BlockingDecision.Block
        assertEquals(BlockReason.IN_FOCUS_SESSION, block.reason)
    }

    @Test
    fun `selective mode - unconfigured app is allowed during session`() {
        val signals = BlockingSignals(
            packageName = testPackage,
            nowEpochMs = nowMs,
            dayOfWeek = 1,
            minuteOfDay = 600,
            sessionState = SessionState.RUNNING,
            blockRule = null,
            isStrictFocusMode = false,
        )

        val decision = BlockingRulesEngine.evaluate(signals)
        assertEquals(BlockingDecision.Allow, decision)
    }

    @Test
    fun `selective mode - app with blockDuringFocus=false is allowed during session`() {
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
            isStrictFocusMode = false,
        )

        val decision = BlockingRulesEngine.evaluate(signals)
        assertEquals(BlockingDecision.Allow, decision)
    }

    // ── Strict Focus Mode ─────────────────────────────────────────────────────

    @Test
    fun `strict mode - any non-safe-listed app is blocked during session`() {
        val signals = BlockingSignals(
            packageName = testPackage,
            nowEpochMs = nowMs,
            dayOfWeek = 1,
            minuteOfDay = 600,
            sessionState = SessionState.RUNNING,
            remainingSessionDurationMs = 1500L,
            blockRule = null, // No explicit rule — still blocked in strict mode
            isStrictFocusMode = true,
        )

        val decision = BlockingRulesEngine.evaluate(signals)
        assertTrue(decision is BlockingDecision.Block)
        val block = decision as BlockingDecision.Block
        assertEquals(BlockReason.STRICT_FOCUS_MODE, block.reason)
    }

    @Test
    fun `strict mode - safe listed package still allowed even in strict mode`() {
        val signals = BlockingSignals(
            packageName = "com.android.dialer",
            nowEpochMs = nowMs,
            dayOfWeek = 1,
            minuteOfDay = 600,
            sessionState = SessionState.RUNNING,
            isDefaultDialer = true,
            isStrictFocusMode = true,
        )

        val decision = BlockingRulesEngine.evaluate(signals)
        assertEquals(BlockingDecision.Allow, decision)
    }

    @Test
    fun `strict mode - launcher is allowed when marked as default launcher`() {
        val signals = BlockingSignals(
            packageName = "com.android.launcher3",
            nowEpochMs = nowMs,
            dayOfWeek = 1,
            minuteOfDay = 600,
            sessionState = SessionState.RUNNING,
            isDefaultLauncher = true, // Launcher is in safe list
            isStrictFocusMode = true,
        )

        // Launcher is safe-listed, so it must always be allowed (AGENTS.md hard constraint)
        val decision = BlockingRulesEngine.evaluate(signals)
        assertEquals(BlockingDecision.Allow, decision)
    }

    // ── Shared invariants ─────────────────────────────────────────────────────

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
