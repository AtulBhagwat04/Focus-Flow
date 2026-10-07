package com.focusflow.core.domain.blocking.model

/**
 * Reasons why an application was blocked by the rules engine.
 */
enum class BlockReason {
    /** Blocked because an active focus session is running. */
    IN_FOCUS_SESSION,

    /** Blocked by a recurring schedule (e.g. bedtime, work hours). */
    ACTIVE_SCHEDULE,

    /** Blocked because today's screen time limit for this app has elapsed. */
    TIME_LIMIT_EXCEEDED,

    /** Blocked because today's allowed launch count for this app has been exceeded. */
    LAUNCH_LIMIT_EXCEEDED,

    /** Always blocked per explicit user rule. */
    ALWAYS_BLOCKED,

    /** Blocked because YouTube Shorts or Instagram Reels feed was detected. */
    SHORT_FORM_FEED,

    /** Blocked in Study Mode because the channel is not on the educational allowlist. */
    STUDY_MODE_CHANNEL_NOT_ALLOWLISTED,
}
