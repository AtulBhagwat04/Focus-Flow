package com.focusflow.core.domain.blocking.engine

import com.focusflow.core.domain.blocking.model.BlockReason
import com.focusflow.core.domain.blocking.model.BlockingDecision
import com.focusflow.core.domain.model.SessionState
import com.focusflow.core.domain.safelist.SafeListPolicy

private const val NEAR_LIMIT_WARNING_THRESHOLD_MS = 5 * 60 * 1000L // 5 minutes
private const val PACKAGE_YOUTUBE = "com.google.android.youtube"
private const val PACKAGE_INSTAGRAM = "com.instagram.android"

/**
 * Pure rules engine evaluating real-time blocking signals against configured policies.
 *
 * Mental Model & Priority Order (Strict):
 * 1. Safe List (Immunity) -> ALWAYS ALLOW
 * 2. Emergency Pass -> ALLOW
 * 3. Explicit Always Blocked -> BLOCK
 * 4. Active Focus Session -> BLOCK
 * 5. Active Schedule -> BLOCK
 * 6. Daily Time Limit Exceeded -> BLOCK
 * 7. Daily Launch Limit Exceeded -> BLOCK
 * 8. Short-Form Video Feed (Shorts/Reels) -> BLOCK
 * 9. YouTube Study Mode -> BLOCK (if channel not allowlisted)
 * 10. Near Limit Warning -> WARN
 * 11. Default -> ALLOW
 */
object BlockingRulesEngine {

    fun evaluate(signals: BlockingSignals): BlockingDecision {
        // Priority 1: Safe List (Absolute priority)
        val isSafe = SafeListPolicy.isSafeListed(
            packageName = signals.packageName,
            isDefaultLauncher = signals.isDefaultLauncher,
            isDefaultDialer = signals.isDefaultDialer,
            userEssentials = signals.userEssentials,
        )
        if (isSafe) return BlockingDecision.Allow

        // Priority 2: Active Emergency Pass
        if (signals.emergencyPass.isActiveAt(signals.nowEpochMs)) {
            return BlockingDecision.Allow
        }

        // Priority 3: Always Blocked
        if (signals.blockRule?.isAlwaysBlocked == true) {
            return BlockingDecision.Block(
                reason = BlockReason.ALWAYS_BLOCKED,
                message = "This app is always blocked during your focus journey.",
            )
        }

        // Priority 4: Active Focus Session
        if (signals.sessionState == SessionState.RUNNING && signals.blockRule?.blockDuringFocus == true) {
            return BlockingDecision.Block(
                reason = BlockReason.IN_FOCUS_SESSION,
                remainingSessionMs = signals.remainingSessionDurationMs,
                message = "You have an active focus session in progress.",
            )
        }

        // Priority 5: Active Schedule
        val activeSchedule = signals.activeSchedules.firstOrNull { schedule ->
            schedule.packageNames.contains(signals.packageName) &&
                schedule.isActiveAt(signals.dayOfWeek, signals.minuteOfDay)
        }
        if (activeSchedule != null) {
            return BlockingDecision.Block(
                reason = BlockReason.ACTIVE_SCHEDULE,
                message = "Blocked by schedule: ${activeSchedule.name}",
            )
        }

        // Priority 6: Daily Time Limit Exceeded
        val limit = signals.dailyLimit
        if (limit?.dailyTimeLimitMs != null && limit.dailyTimeLimitMs > 0L) {
            val remainingTimeMs = limit.dailyTimeLimitMs - signals.consumedDurationTodayMs
            if (remainingTimeMs <= 0L) {
                return BlockingDecision.Block(
                    reason = BlockReason.TIME_LIMIT_EXCEEDED,
                    message = "Daily limit reached for this app.",
                )
            }
        }

        // Priority 7: Daily Launch Limit Exceeded
        if (limit?.dailyLaunchLimit != null && limit.dailyLaunchLimit > 0) {
            if (signals.launchCountToday >= limit.dailyLaunchLimit) {
                return BlockingDecision.Block(
                    reason = BlockReason.LAUNCH_LIMIT_EXCEEDED,
                    message = "Daily launch limit reached for this app.",
                )
            }
        }

        // Priority 8: Short-Form Video Feed (Shorts / Reels)
        val advConfig = signals.advancedBlockingConfig
        if (signals.isShortFormFeedDetected && advConfig != null && !advConfig.isKillSwitchActive) {
            val shouldEnforceSession = !advConfig.blockDuringFocusOnly || signals.sessionState == SessionState.RUNNING
            if (shouldEnforceSession) {
                if (signals.packageName == PACKAGE_YOUTUBE && advConfig.isShortsBlockingEnabled) {
                    return BlockingDecision.Block(
                        reason = BlockReason.SHORT_FORM_FEED,
                        message = "YouTube Shorts is restricted during focus mode.",
                    )
                }
                if (signals.packageName == PACKAGE_INSTAGRAM && advConfig.isReelsBlockingEnabled) {
                    return BlockingDecision.Block(
                        reason = BlockReason.SHORT_FORM_FEED,
                        message = "Instagram Reels is restricted during focus mode.",
                    )
                }
            }
        }

        // Priority 9: YouTube Study Mode
        val studyConfig = signals.studyModeConfig
        if (signals.packageName == PACKAGE_YOUTUBE && studyConfig?.isEnabled == true && !studyConfig.isKillSwitchActive) {
            val detectedChannel = signals.detectedChannelName?.trim()
            val detectedChannelId = signals.detectedChannelId?.trim()

            val isChannelAllowlisted = when {
                !detectedChannelId.isNullOrBlank() && studyConfig.allowlistedChannelIds.contains(detectedChannelId) -> true
                !detectedChannel.isNullOrBlank() && studyConfig.allowlistedChannelNames.any {
                    it.equals(detectedChannel, ignoreCase = true)
                } -> true
                else -> false
            }

            if (!isChannelAllowlisted) {
                if (detectedChannel != null || detectedChannelId != null) {
                    return BlockingDecision.Block(
                        reason = BlockReason.STUDY_MODE_CHANNEL_NOT_ALLOWLISTED,
                        message = "YouTube Study Mode: Channel '$detectedChannel' is not on your allowlist.",
                    )
                } else if (studyConfig.failClosed) {
                    return BlockingDecision.Block(
                        reason = BlockReason.STUDY_MODE_CHANNEL_NOT_ALLOWLISTED,
                        message = "YouTube Study Mode: Channel could not be verified (Fail-Closed active).",
                    )
                }
            }
        }

        // Priority 10: Near Limit Warning
        if (limit?.dailyTimeLimitMs != null && limit.dailyTimeLimitMs > 0L) {
            val remainingMs = limit.dailyTimeLimitMs - signals.consumedDurationTodayMs
            if (remainingMs in 1..NEAR_LIMIT_WARNING_THRESHOLD_MS) {
                return BlockingDecision.WarnNearLimit(remainingLimitMs = remainingMs)
            }
        }

        // Priority 11: Default Allow
        return BlockingDecision.Allow
    }
}
