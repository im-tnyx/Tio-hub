package com.tnyx.features.auth.presentation.login

import com.tnyx.features.auth.domain.model.AuthResult
import com.tnyx.features.auth.domain.model.AuthSession
import com.tnyx.features.auth.domain.repository.TestAuthRepository
import com.tnyx.features.auth.presentation.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = TestAuthRepository()
    private val viewModel = LoginViewModel(authRepository)

    @Test
    fun handleAction_EmailChanged_updatesState() {
        viewModel.handleAction(LoginAction.EmailChanged("test@example.com"))
        assertEquals("test@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun handleAction_PasswordChanged_updatesState() {
        viewModel.handleAction(LoginAction.PasswordChanged("password"))
        assertEquals("password", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun submit_withBlankEmail_showsError() {
        viewModel.handleAction(LoginAction.PasswordChanged("password123"))
        viewModel.handleAction(LoginAction.SignInClicked)
        assertEquals("Email is required", viewModel.uiState.value.emailError)
    }

    @Test
    fun submit_withInvalidEmailFormat_showsError() {
        viewModel.handleAction(LoginAction.EmailChanged("invalid-email"))
        viewModel.handleAction(LoginAction.PasswordChanged("password123"))
        viewModel.handleAction(LoginAction.SignInClicked)
        assertEquals("Enter a valid email", viewModel.uiState.value.emailError)
    }

    @Test
    fun submit_withBlankPassword_showsError() {
        viewModel.handleAction(LoginAction.EmailChanged("test@example.com"))
        viewModel.handleAction(LoginAction.SignInClicked)
        assertEquals("Password is required", viewModel.uiState.value.passwordError)
    }

    @Test
    fun submit_withShortPassword_showsError() {
        viewModel.handleAction(LoginAction.EmailChanged("test@example.com"))
        viewModel.handleAction(LoginAction.PasswordChanged("123"))
        viewModel.handleAction(LoginAction.SignInClicked)
        assertEquals("Password must be at least 6 characters", viewModel.uiState.value.passwordError)
    }

    @Test
    fun submit_successfulAuth_updatesLoadingAndEmitsEffect() = runTest {
        authRepository.signInResult = AuthResult.Authenticated(
            AuthSession("uid", "test@example.com", "User", false)
        )
        authRepository.delayMs = 1000

        val effects = mutableListOf<LoginEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.handleAction(LoginAction.EmailChanged("test@example.com"))
        viewModel.handleAction(LoginAction.PasswordChanged("password123"))
        viewModel.handleAction(LoginAction.SignInClicked)

        runCurrent()

        // Loading is set to true immediately
        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        // Loading is set back to false
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.emailError)
        assertNull(viewModel.uiState.value.passwordError)

        assertEquals(1, effects.size)
        assertEquals(LoginEffect.Authenticated, effects[0])

        collectJob.cancel()
    }

    @Test
    fun submit_authFailure_showsError() = runTest {
        authRepository.signInResult = AuthResult.Failure("Invalid credentials")

        viewModel.handleAction(LoginAction.EmailChanged("test@example.com"))
        viewModel.handleAction(LoginAction.PasswordChanged("password123"))
        viewModel.handleAction(LoginAction.SignInClicked)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Invalid credentials", viewModel.uiState.value.passwordError)
    }

    @Test
    fun submit_verificationRequired_showsError() = runTest {
        authRepository.signInResult = AuthResult.VerificationRequired("test@example.com")

        viewModel.handleAction(LoginAction.EmailChanged("test@example.com"))
        viewModel.handleAction(LoginAction.PasswordChanged("password123"))
        viewModel.handleAction(LoginAction.SignInClicked)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Verify your email before signing in", viewModel.uiState.value.emailError)
    }

    @Test
    fun demoSignIn_successfulAuth_updatesLoadingAndEmitsEffect() = runTest {
        authRepository.signInWithDemoResult = AuthResult.Authenticated(
            AuthSession("demo-uid", "demo@tnyx.app", "Demo User", true)
        )
        authRepository.delayMs = 1000

        val effects = mutableListOf<LoginEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.handleAction(LoginAction.DemoAccountClicked)

        runCurrent()

        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.emailError)
        assertNull(viewModel.uiState.value.passwordError)

        assertEquals(1, effects.size)
        assertEquals(LoginEffect.Authenticated, effects[0])

        collectJob.cancel()
    }

    @Test
    fun handleAction_CreateAccountClicked_emitsNavigateToSignup() = runTest {
        val copyEffects = mutableListOf<LoginEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { copyEffects.add(it) }
        }

        viewModel.handleAction(LoginAction.CreateAccountClicked)
        advanceUntilIdle()

        assertEquals(1, copyEffects.size)
        assertEquals(LoginEffect.NavigateToSignup, copyEffects[0])

        collectJob.cancel()
    }
}
