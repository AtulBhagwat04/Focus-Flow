package com.focusflow.core.domain.auth.model

/**
 * Domain representation of an authenticated user account.
 * Pure Kotlin, decoupled from Firebase Auth.
 */
data class UserAccount(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val isAnonymous: Boolean = true,
    val photoUrl: String? = null,
)
