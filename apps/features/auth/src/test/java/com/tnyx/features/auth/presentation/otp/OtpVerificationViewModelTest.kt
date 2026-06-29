package com.tnyx.features.auth.presentation.otp

import androidx.lifecycle.SavedStateHandle
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
class OtpVerificationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = TestAuthRepository()

    private fun createViewModel(email: String = "test@example.com"): OtpVerificationViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("email" to email))
        return OtpVerificationViewModel(savedStateHandle, authRepository)
    }

    @Test
    fun init_retrievesEmailFromSavedStateHandle() {
        val viewModel = createViewModel("init@example.com")
        assertEquals("init@example.com", viewModel.uiState.value.email)
    }

    @Test
    fun handleAction_CodeChanged_filtersDigitsAndLimitsLength() {
        val viewModel = createViewModel()
        viewModel.handleAction(OtpVerificationAction.CodeChanged("12a34567"))
        assertEquals("123456", viewModel.uiState.value.code)
        assertNull(viewModel.uiState.value.codeError)
        assertNull(viewModel.uiState.value.statusMessage)
    }

    @Test
    fun verify_withShortCode_showsError() {
        val viewModel = createViewModel()
        viewModel.handleAction(OtpVerificationAction.CodeChanged("12345"))
        viewModel.handleAction(OtpVerificationAction.VerifyClicked)
        assertEquals("Enter the 6-digit code", viewModel.uiState.value.codeError)
    }

    @Test
    fun verify_successfulAuth_updatesLoadingAndEmitsEffect() = runTest {
        val viewModel = createViewModel("test@example.com")
        authRepository.verifyOtpResult = AuthResult.Authenticated(
            AuthSession("uid", "test@example.com", "User", false)
        )

        val effects = mutableListOf<OtpVerificationEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        authRepository.delayMs = 1000
        viewModel.handleAction(OtpVerificationAction.CodeChanged("123456"))
        viewModel.handleAction(OtpVerificationAction.VerifyClicked)

        runCurrent()

        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.codeError)

        assertEquals(1, effects.size)
        assertEquals(OtpVerificationEffect.Authenticated, effects[0])

        collectJob.cancel()
    }

    @Test
    fun verify_verificationRequired_showsStatusMessage() = runTest {
        val viewModel = createViewModel("test@example.com")
        authRepository.verifyOtpResult = AuthResult.VerificationRequired("test@example.com")

        viewModel.handleAction(OtpVerificationAction.CodeChanged("123456"))
        viewModel.handleAction(OtpVerificationAction.VerifyClicked)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Verification code sent again", viewModel.uiState.value.statusMessage)
    }

    @Test
    fun verify_failure_showsError() = runTest {
        val viewModel = createViewModel("test@example.com")
        authRepository.verifyOtpResult = AuthResult.Failure("Invalid code")

        viewModel.handleAction(OtpVerificationAction.CodeChanged("123456"))
        viewModel.handleAction(OtpVerificationAction.VerifyClicked)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Invalid code", viewModel.uiState.value.codeError)
    }

    @Test
    fun resend_verificationRequired_showsStatusMessage() = runTest {
        val viewModel = createViewModel("test@example.com")
        authRepository.resendOtpResult = AuthResult.VerificationRequired("test@example.com")

        authRepository.delayMs = 1000
        viewModel.handleAction(OtpVerificationAction.ResendClicked)

        runCurrent()

        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Verification code sent again", viewModel.uiState.value.statusMessage)
        assertNull(viewModel.uiState.value.codeError)
    }

    @Test
    fun resend_failure_showsError() = runTest {
        val viewModel = createViewModel("test@example.com")
        authRepository.resendOtpResult = AuthResult.Failure("Too many requests")

        viewModel.handleAction(OtpVerificationAction.ResendClicked)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Too many requests", viewModel.uiState.value.codeError)
        assertNull(viewModel.uiState.value.statusMessage)
    }

    @Test
    fun handleAction_BackClicked_emitsNavigateBack() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<OtpVerificationEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.handleAction(OtpVerificationAction.BackClicked)
        advanceUntilIdle()

        assertEquals(1, effects.size)
        assertEquals(OtpVerificationEffect.NavigateBack, effects[0])

        collectJob.cancel()
    }
}
