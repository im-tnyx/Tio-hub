package com.tnyx.features.splash.presentation.view_model

import com.tnyx.features.splash.presentation.action.SplashAction
import com.tnyx.features.splash.presentation.state.SplashEffect
import com.tnyx.shared.auth.domain.model.AuthSession
import com.tnyx.shared.auth.domain.repository.AuthSessionProvider
import com.tnyx.shared.profile.domain.model.UserProfile
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

import com.tnyx.features.auth.domain.model.AuthResult
import com.tnyx.features.auth.domain.repository.AuthRepository

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun signedOutSessionAutoSignsInAnonymouslyAndNavigatesToWelcome() = runTest {
        val viewModel = SplashViewModel(
            TestSessionProvider(null),
            TestProfileRepository(),
            TestAuthRepository(),
        )
        val effects = mutableListOf<SplashEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleAction(SplashAction.Init)
        advanceUntilIdle()

        assertEquals(listOf(SplashEffect.NavigateToWelcome), effects)
        assertFalse(viewModel.uiState.value.isLoading)
        collectJob.cancel()
    }

    @Test
    fun failedAnonymousSignInNavigatesToWelcome() = runTest {
        val viewModel = SplashViewModel(
            TestSessionProvider(null),
            TestProfileRepository(),
            FailingAuthRepository(),
        )
        val effects = mutableListOf<SplashEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleAction(SplashAction.Init)
        advanceUntilIdle()

        assertEquals(listOf(SplashEffect.NavigateToWelcome), effects)
        assertFalse(viewModel.uiState.value.isLoading)
        collectJob.cancel()
    }

    @Test
    fun persistedSessionWithoutCompletedOnboardingNavigatesToOnboarding() = runTest {
        val viewModel = SplashViewModel(
            TestSessionProvider(session()),
            TestProfileRepository(),
            TestAuthRepository(),
        )
        val effects = mutableListOf<SplashEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleAction(SplashAction.Init)
        advanceUntilIdle()

        assertEquals(listOf(SplashEffect.NavigateToOnboarding), effects)
        assertFalse(viewModel.uiState.value.isLoading)
        collectJob.cancel()
    }

    @Test
    fun persistedGuestSessionWithoutCompletedOnboardingNavigatesToWelcome() = runTest {
        val viewModel = SplashViewModel(
            TestSessionProvider(
                AuthSession(
                    userId = "guest-user",
                    email = "guest@tnyx.app",
                    displayName = "Guest User",
                    isDemo = true,
                ),
            ),
            TestProfileRepository(),
            TestAuthRepository(),
        )
        val effects = mutableListOf<SplashEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleAction(SplashAction.Init)
        advanceUntilIdle()

        assertEquals(listOf(SplashEffect.NavigateToWelcome), effects)
        assertFalse(viewModel.uiState.value.isLoading)
        collectJob.cancel()
    }

    @Test
    fun restoredGuestSessionWithoutStoredSessionNavigatesToWelcome() = runTest {
        val authRepository = TestAuthRepository().apply {
            restoreSessionResult = AuthSession(
                userId = "restored-guest",
                email = "guest@tnyx.app",
                displayName = "Guest User",
                isDemo = true,
            )
        }
        val viewModel = SplashViewModel(
            TestSessionProvider(null),
            TestProfileRepository(),
            authRepository,
        )
        val effects = mutableListOf<SplashEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleAction(SplashAction.Init)
        advanceUntilIdle()

        assertEquals(listOf(SplashEffect.NavigateToWelcome), effects)
        assertFalse(viewModel.uiState.value.isLoading)
        collectJob.cancel()
    }

    @Test
    fun repeatedInitEmitsOnlyOneNavigationEffect() = runTest {
        val viewModel = SplashViewModel(
            TestSessionProvider(session()),
            TestProfileRepository(),
            TestAuthRepository(),
        )
        val effects = mutableListOf<SplashEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleAction(SplashAction.Init)
        viewModel.handleAction(SplashAction.Init)
        advanceUntilIdle()

        assertEquals(listOf(SplashEffect.NavigateToOnboarding), effects)
        collectJob.cancel()
    }

    @Test
    fun completedGuestOnboardingNavigatesToMain() = runTest {
        val viewModel = SplashViewModel(
            TestSessionProvider(null),
            TestProfileRepository(
                UserProfile(
                    id = "local-guest",
                    displayName = "Santosh",
                    dob = "1990-01-01",
                    gender = "male",
                    planLabel = "",
                    weight = 72.0,
                    height = 175,
                    bmi = 0.0,
                    bmr = 0,
                    hasCompletedOnboarding = true,
                ),
            ),
            TestAuthRepository(),
        )
        val effects = mutableListOf<SplashEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleAction(SplashAction.Init)
        advanceUntilIdle()

        assertEquals(listOf(SplashEffect.NavigateToMain), effects)
        collectJob.cancel()
    }

    private fun session(): AuthSession {
        return AuthSession(
            userId = "persistent-user",
            email = "persistent@example.com",
            displayName = "Persistent User",
            isDemo = false,
        )
    }
}

private class TestSessionProvider(
    private val session: AuthSession?,
) : AuthSessionProvider {
    override fun observeSession(): Flow<AuthSession?> = flowOf(session)

    override fun currentSession(): AuthSession? = session
}

private class TestProfileRepository(
    private val profile: UserProfile = UserProfile(
        id = "local-guest",
        displayName = "",
        dob = "",
        gender = "",
        planLabel = "",
        weight = 0.0,
        height = 0,
        bmi = 0.0,
        bmr = 0,
    ),
) : ProfileRepository {
    override fun getCurrentProfile(): Flow<UserProfile> = flowOf(profile)

    override fun getProfile(userId: String): Flow<UserProfile> = flowOf(profile)

    override suspend fun updateProfile(profile: UserProfile) = Unit

    override suspend fun updateAvatar(jpegBytes: ByteArray): String = ""

    override suspend fun removeAvatar() = Unit
}

private class TestAuthRepository : AuthRepository {
    var restoreSessionResult: AuthSession? = null

    override suspend fun signIn(email: String, password: String): AuthResult = AuthResult.Failure("")
    override suspend fun signInWithGoogle(): AuthResult = AuthResult.Failure("")
    override suspend fun signInWithDemoAccount(): AuthResult = AuthResult.Failure("")
    override suspend fun signUp(name: String, email: String, password: String): AuthResult = AuthResult.Failure("")
    override suspend fun verifyOtp(email: String, code: String): AuthResult = AuthResult.Failure("")
    override suspend fun resendOtp(email: String): AuthResult = AuthResult.Failure("")
    override suspend fun restoreSessionIfAvailable(): AuthSession? = restoreSessionResult
    override suspend fun signInAnonymously(): AuthResult = AuthResult.Authenticated(
        AuthSession(userId = "anon-user", email = "guest@tnyx.app", displayName = "Guest User", isDemo = true)
    )
    override suspend fun signOut() = Unit
}

private class FailingAuthRepository : AuthRepository {
    override suspend fun signIn(email: String, password: String): AuthResult = AuthResult.Failure("")
    override suspend fun signInWithGoogle(): AuthResult = AuthResult.Failure("")
    override suspend fun signInWithDemoAccount(): AuthResult = AuthResult.Failure("")
    override suspend fun signUp(name: String, email: String, password: String): AuthResult = AuthResult.Failure("")
    override suspend fun verifyOtp(email: String, code: String): AuthResult = AuthResult.Failure("")
    override suspend fun resendOtp(email: String): AuthResult = AuthResult.Failure("")
    override suspend fun restoreSessionIfAvailable(): AuthSession? = null
    override suspend fun signInAnonymously(): AuthResult = AuthResult.Failure("Network error")
    override suspend fun signOut() = Unit
}
