package com.tnyx.features.auth.presentation.signup

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
class SignupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = TestAuthRepository()
    private val viewModel = SignupViewModel(authRepository)

    @Test
    fun handleAction_NameChanged_updatesState() {
        viewModel.handleAction(SignupAction.NameChanged("John Doe"))
        assertEquals("John Doe", viewModel.uiState.value.name)
        assertNull(viewModel.uiState.value.nameError)
    }

    @Test
    fun handleAction_EmailChanged_updatesState() {
        viewModel.handleAction(SignupAction.EmailChanged("test@example.com"))
        assertEquals("test@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun handleAction_PasswordChanged_updatesState() {
        viewModel.handleAction(SignupAction.PasswordChanged("secure123"))
        assertEquals("secure123", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun submit_withBlankName_showsError() {
        viewModel.handleAction(SignupAction.EmailChanged("test@example.com"))
        viewModel.handleAction(SignupAction.PasswordChanged("secure123"))
        viewModel.handleAction(SignupAction.SignupClicked)
        assertEquals("Name is required", viewModel.uiState.value.nameError)
    }

    @Test
    fun submit_withBlankEmail_showsError() {
        viewModel.handleAction(SignupAction.NameChanged("John Doe"))
        viewModel.handleAction(SignupAction.PasswordChanged("secure123"))
        viewModel.handleAction(SignupAction.SignupClicked)
        assertEquals("Email is required", viewModel.uiState.value.emailError)
    }

    @Test
    fun submit_withInvalidEmail_showsError() {
        viewModel.handleAction(SignupAction.NameChanged("John Doe"))
        viewModel.handleAction(SignupAction.EmailChanged("invalid-email"))
        viewModel.handleAction(SignupAction.PasswordChanged("secure123"))
        viewModel.handleAction(SignupAction.SignupClicked)
        assertEquals("Enter a valid email", viewModel.uiState.value.emailError)
    }

    @Test
    fun submit_withBlankPassword_showsError() {
        viewModel.handleAction(SignupAction.NameChanged("John Doe"))
        viewModel.handleAction(SignupAction.EmailChanged("test@example.com"))
        viewModel.handleAction(SignupAction.SignupClicked)
        assertEquals("Password is required", viewModel.uiState.value.passwordError)
    }

    @Test
    fun submit_withShortPassword_showsError() {
        viewModel.handleAction(SignupAction.NameChanged("John Doe"))
        viewModel.handleAction(SignupAction.EmailChanged("test@example.com"))
        viewModel.handleAction(SignupAction.PasswordChanged("123"))
        viewModel.handleAction(SignupAction.SignupClicked)
        assertEquals("Password must be at least 6 characters", viewModel.uiState.value.passwordError)
    }

    @Test
    fun submit_successfulAuthenticated_updatesLoadingAndEmitsEffect() = runTest {
        authRepository.signUpResult = AuthResult.Authenticated(
            AuthSession("uid", "test@example.com", "John Doe", false)
        )

        val effects = mutableListOf<SignupEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.handleAction(SignupAction.NameChanged("John Doe"))
        viewModel.handleAction(SignupAction.EmailChanged("test@example.com"))
        viewModel.handleAction(SignupAction.PasswordChanged("secure123"))
        authRepository.delayMs = 1000
        viewModel.handleAction(SignupAction.SignupClicked)

        runCurrent()

        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.nameError)
        assertNull(viewModel.uiState.value.emailError)
        assertNull(viewModel.uiState.value.passwordError)

        assertEquals(1, effects.size)
        assertEquals(SignupEffect.Authenticated, effects[0])

        collectJob.cancel()
    }

    @Test
    fun submit_verificationRequired_updatesLoadingAndEmitsNavigateToOtp() = runTest {
        authRepository.signUpResult = AuthResult.VerificationRequired("test@example.com")

        val effects = mutableListOf<SignupEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.handleAction(SignupAction.NameChanged("John Doe"))
        viewModel.handleAction(SignupAction.EmailChanged("test@example.com"))
        viewModel.handleAction(SignupAction.PasswordChanged("secure123"))
        viewModel.handleAction(SignupAction.SignupClicked)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)

        assertEquals(1, effects.size)
        assertEquals(SignupEffect.NavigateToOtp("test@example.com"), effects[0])

        collectJob.cancel()
    }

    @Test
    fun submit_signUpFailure_showsError() = runTest {
        authRepository.signUpResult = AuthResult.Failure("Email already in use")

        viewModel.handleAction(SignupAction.NameChanged("John Doe"))
        viewModel.handleAction(SignupAction.EmailChanged("test@example.com"))
        viewModel.handleAction(SignupAction.PasswordChanged("secure123"))
        viewModel.handleAction(SignupAction.SignupClicked)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Email already in use", viewModel.uiState.value.passwordError)
    }

    @Test
    fun handleAction_LoginClicked_emitsNavigateToLogin() = runTest {
        val effects = mutableListOf<SignupEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.handleAction(SignupAction.LoginClicked)
        advanceUntilIdle()

        assertEquals(1, effects.size)
        assertEquals(SignupEffect.NavigateToLogin, effects[0])

        collectJob.cancel()
    }
}
