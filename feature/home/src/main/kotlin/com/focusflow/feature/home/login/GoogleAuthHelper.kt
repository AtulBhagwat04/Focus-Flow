package com.focusflow.feature.home.login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Exception indicating the user dismissed or cancelled the Google Sign-In prompt.
 */
class UserCancelledLoginException(message: String = "Sign in cancelled") : Exception(message)

/**
 * Exception indicating that Google Sign-In is not properly configured.
 */
class GoogleAuthConfigurationException(
    message: String = "Google Web Client ID is not configured. Please add SHA-1 to Firebase and update google-services.json.",
) : Exception(message)

/**
 * Exception indicating that no Google credentials were found or available on the device.
 */
class NoCredentialsFoundException(
    message: String = "No Google credentials available. Ensure a Google account is logged into the device and your SHA-1 fingerprint is registered in Firebase.",
) : Exception(message)

/**
 * Helper class that coordinates with Android's Credential Manager to request
 * a Google ID token.
 */
class GoogleAuthHelper(
    private val context: Context,
) {
    private val credentialManager = CredentialManager.create(context)

    /**
     * Attempts to retrieve a Google ID token for the given [serverClientId].
     */
    suspend fun getGoogleIdToken(serverClientId: String): Result<String> {
        if (serverClientId.isBlank() || isInvalidServerClientId(serverClientId)) {
            return Result.failure(
                GoogleAuthConfigurationException(
                    "Google Web Client ID is missing or invalid. Please configure Google Sign-In in Firebase Console and update google-services.json."
                )
            )
        }

        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = context,
            )

            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Result.success(googleIdTokenCredential.idToken)
            } else {
                Result.failure(IllegalStateException("Unsupported credential type: ${credential.type}"))
            }
        } catch (_: GetCredentialCancellationException) {
            Result.failure(UserCancelledLoginException())
        } catch (_: NoCredentialException) {
            Result.failure(
                NoCredentialsFoundException(
                    "No Google credentials available. Ensure a Google account is logged into the device and your SHA-1 fingerprint is registered in Firebase."
                )
            )
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isInvalidServerClientId(clientId: String): Boolean {
        // Valid OAuth 2.0 Web Client IDs follow: "<project_number>-<client_hash>.apps.googleusercontent.com"
        val trimmed = clientId.trim()
        val suffix = ".apps.googleusercontent.com"
        if (!trimmed.endsWith(suffix)) return true
        val prefix = trimmed.removeSuffix(suffix)
        return !prefix.contains("-") || prefix.contains("placeholder", ignoreCase = true)
    }
}

