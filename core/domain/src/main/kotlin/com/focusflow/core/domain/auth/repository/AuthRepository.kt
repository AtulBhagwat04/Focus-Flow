package com.focusflow.core.domain.auth.repository

import com.focusflow.core.domain.auth.model.UserAccount
import com.focusflow.core.domain.auth.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Access to authentication state, anonymous account initialization, Google linking, and account deletion.
 */
interface AuthRepository {
    val currentUser: Flow<UserAccount?>
    val userProfile: Flow<UserProfile?>

    suspend fun signInAnonymously(): Result<UserAccount>
    suspend fun linkGoogleAccount(idToken: String): Result<UserAccount>
    suspend fun signOut(): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
}
