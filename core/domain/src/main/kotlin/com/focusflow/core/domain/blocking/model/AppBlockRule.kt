package com.focusflow.core.domain.blocking.model

/**
 * Blocking behavior configuration for a single package.
 */
data class AppBlockRule(
    val packageName: String,
    val blockDuringFocus: Boolean = true,
    val isAlwaysBlocked: Boolean = false,
)
