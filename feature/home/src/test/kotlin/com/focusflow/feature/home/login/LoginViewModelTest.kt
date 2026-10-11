package com.focusflow.feature.home.login

import com.focusflow.core.domain.auth.model.UserAccount
import com.focusflow.core.domain.auth.model.UserProfile
import com.focusflow.core.domain.auth.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        viewModel = LoginViewModel(authRepository = fakeAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isIdleAndUnauthenticated() {
        val state = viewModel.uiState.value
        assertFalse(state.isGoogleLoading)
        assertFalse(state.isGuestLoading)
        assertNull(state.errorMessage)
        assertFalse(state.isAuthenticated)
        assertFalse(state.showTermsDialog)
        assertFalse(state.showPrivacyDialog)
    }

    @Test
    fun onGoogleSignInStarted_setsGoogleLoading() {
        viewModel.onGoogleSignInStarted()
        assertTrue(viewModel.uiState.value.isGoogleLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onGoogleIdTokenReceived_success_updatesStateAndNavigates() = runTest {
        fakeAuthRepository.googleSignInResult = Result.success(
            UserAccount(
                uid = "test-google-uid",
                displayName = "Focus User",
                email = "user@example.com",
                isAnonymous = false,
            )
        )

        viewModel.onGoogleIdTokenReceived("valid_google_token")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isGoogleLoading)
        assertTrue(state.isAuthenticated)
        assertNull(state.errorMessage)

        val effect = viewModel.effects.first()
        assertEquals(LoginEffect.NavigateForward, effect)
    }

    @Test
    fun onGoogleIdTokenReceived_failure_setsErrorMessage() = runTest {
        fakeAuthRepository.googleSignInResult = Result.failure(RuntimeException("Network failure"))

        viewModel.onGoogleIdTokenReceived("invalid_token")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isGoogleLoading)
        assertFalse(state.isAuthenticated)
        assertEquals("Network failure", state.errorMessage)
    }

    @Test
    fun onGoogleSignInError_userCancelled_doesNotSetErrorMessage() {
        viewModel.onGoogleSignInStarted()
        viewModel.onGoogleSignInError(UserCancelledLoginException())

        val state = viewModel.uiState.value
        assertFalse(state.isGoogleLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun onGoogleSignInError_actualException_setsErrorMessage() {
        viewModel.onGoogleSignInStarted()
        viewModel.onGoogleSignInError(IllegalStateException("No credentials available"))

        val state = viewModel.uiState.value
        assertFalse(state.isGoogleLoading)
        assertEquals("No credentials available", state.errorMessage)
    }

    @Test
    fun onGoogleSignInError_configurationMissing_setsHelpfulErrorMessage() {
        viewModel.onGoogleSignInStarted()
        val exception = GoogleAuthConfigurationException()
        viewModel.onGoogleSignInError(exception)

        val state = viewModel.uiState.value
        assertFalse(state.isGoogleLoading)
        assertEquals(exception.message, state.errorMessage)
    }

    @Test
    fun onGoogleSignInError_noCredentialsFound_setsHelpfulErrorMessage() {
        viewModel.onGoogleSignInStarted()
        val exception = NoCredentialsFoundException()
        viewModel.onGoogleSignInError(exception)

        val state = viewModel.uiState.value
        assertFalse(state.isGoogleLoading)
        assertEquals(exception.message, state.errorMessage)
    }

    @Test
    fun onGuestSignIn_success_updatesStateAndNavigates() = runTest {
        fakeAuthRepository.anonymousSignInResult = Result.success(
            UserAccount(
                uid = "test-guest-uid",
                displayName = null,
                email = null,
                isAnonymous = true,
            )
        )

        viewModel.onGuestSignIn()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isGuestLoading)
        assertTrue(state.isAuthenticated)
        assertNull(state.errorMessage)

        val effect = viewModel.effects.first()
        assertEquals(LoginEffect.NavigateForward, effect)
    }

    @Test
    fun onGuestSignIn_failure_setsErrorMessage() = runTest {
        fakeAuthRepository.anonymousSignInResult = Result.failure(RuntimeException("Auth disabled"))

        viewModel.onGuestSignIn()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isGuestLoading)
        assertFalse(state.isAuthenticated)
        assertEquals("Auth disabled", state.errorMessage)
    }

    @Test
    fun termsAndPrivacyDialogs_toggleCorrectly() {
        viewModel.onShowTerms()
        assertTrue(viewModel.uiState.value.showTermsDialog)
        viewModel.onDismissTerms()
        assertFalse(viewModel.uiState.value.showTermsDialog)

        viewModel.onShowPrivacy()
        assertTrue(viewModel.uiState.value.showPrivacyDialog)
        viewModel.onDismissPrivacy()
        assertFalse(viewModel.uiState.value.showPrivacyDialog)
    }
}

private class FakeAuthRepository : AuthRepository {
    var googleSignInResult: Result<UserAccount> = Result.success(
        UserAccount(uid = "default-uid", isAnonymous = false)
    )
    var anonymousSignInResult: Result<UserAccount> = Result.success(
        UserAccount(uid = "guest-uid", isAnonymous = true)
    )

    override val currentUser: kotlinx.coroutines.flow.StateFlow<UserAccount?> =
        kotlinx.coroutines.flow.MutableStateFlow(null)
    override val userProfile: Flow<UserProfile?> = flowOf(null)

    override suspend fun signInAnonymously(): Result<UserAccount> = anonymousSignInResult

    override suspend fun signInWithGoogle(idToken: String): Result<UserAccount> = googleSignInResult

    override suspend fun linkGoogleAccount(idToken: String): Result<UserAccount> = googleSignInResult

    override suspend fun signOut(): Result<Unit> = Result.success(Unit)

    override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
}
