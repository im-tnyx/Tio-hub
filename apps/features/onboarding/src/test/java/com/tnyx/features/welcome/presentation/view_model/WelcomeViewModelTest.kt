package com.tnyx.features.welcome.presentation.view_model

import com.tnyx.features.onboarding.presentation.MainDispatcherRule
import com.tnyx.features.auth.domain.model.AuthResult
import com.tnyx.features.auth.domain.repository.AuthRepository
import com.tnyx.features.welcome.presentation.action.WelcomeAction
import com.tnyx.features.welcome.presentation.state.WelcomeEffect
import com.tnyx.shared.auth.domain.model.AuthSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WelcomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun getStartedNavigatesToOnboarding() = runTest {
        assertEffect(
            action = WelcomeAction.GetStartedClicked,
            expected = WelcomeEffect.NavigateToOnboarding,
            authRepository = FakeAuthRepository(),
        )
    }

    @Test
    fun skipNavigatesDirectlyToMain() = runTest {
        val authRepository = FakeAuthRepository().apply {
            signInWithDemoResult = AuthResult.Authenticated(
                AuthSession("demo-uid", "demo@tnyx.app", "Demo User", false),
            )
        }
        assertEffect(
            action = WelcomeAction.SkipForNowClicked,
            expected = WelcomeEffect.NavigateToMain,
            authRepository = authRepository,
        )
    }

    @Test
    fun signInNavigatesToLogin() = runTest {
        assertEffect(
            action = WelcomeAction.SignInClicked,
            expected = WelcomeEffect.NavigateToLogin,
            authRepository = FakeAuthRepository(),
        )
    }

    @Test
    fun skipFailureSurfacesErrorWithoutNavigation() = runTest {
        val authRepository = FakeAuthRepository().apply {
            signInWithDemoResult = AuthResult.Failure("Demo account is not configured on this build")
        }
        val viewModel = WelcomeViewModel(authRepository)
        val effects = mutableListOf<WelcomeEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleAction(WelcomeAction.SkipForNowClicked)
        advanceUntilIdle()

        assertTrue(effects.isEmpty())
        assertEquals("Demo account is not configured on this build", viewModel.uiState.value.skipError)
        collectJob.cancel()
    }

    private suspend fun kotlinx.coroutines.test.TestScope.assertEffect(
        action: WelcomeAction,
        expected: WelcomeEffect,
        authRepository: FakeAuthRepository,
    ) {
        val viewModel = WelcomeViewModel(authRepository)
        val effects = mutableListOf<WelcomeEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleAction(action)
        advanceUntilIdle()

        assertEquals(listOf(expected), effects)
        collectJob.cancel()
    }

    private class FakeAuthRepository : AuthRepository {
        var signInWithDemoResult: AuthResult = AuthResult.Failure("Not initialized")

        override suspend fun signIn(email: String, password: String): AuthResult {
            return AuthResult.Failure("Unused in this test")
        }

        override suspend fun signInWithGoogle(): AuthResult {
            return AuthResult.Failure("Unused in this test")
        }

        override suspend fun signInWithDemoAccount(): AuthResult {
            return signInWithDemoResult
        }

        override suspend fun signUp(name: String, email: String, password: String): AuthResult {
            return AuthResult.Failure("Unused in this test")
        }

        override suspend fun verifyOtp(email: String, code: String): AuthResult {
            return AuthResult.Failure("Unused in this test")
        }

        override suspend fun resendOtp(email: String): AuthResult {
            return AuthResult.Failure("Unused in this test")
        }

        override suspend fun restoreSessionIfAvailable(): AuthSession? = null

        override suspend fun signInAnonymously(): AuthResult {
            return AuthResult.Failure("Unused in this test")
        }

        override suspend fun signOut() = Unit
    }
}
