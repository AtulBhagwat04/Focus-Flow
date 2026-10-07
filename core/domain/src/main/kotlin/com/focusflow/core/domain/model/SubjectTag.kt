package com.focusflow.core.domain.model

/**
 * Subject tag used to categorize focus sessions.
 *
 * Per PROJECT_BRIEF.md §3.2:
 * "Subject tags (for example Maths, Coding, Revision) used to group stats."
 */
data class SubjectTag(
    val id: String,
    val name: String,
    val colorHex: String,
    val iconName: String = "timer",
    val isDefault: Boolean = false,
)
