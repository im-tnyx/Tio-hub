package com.tnyx.features.splash.presentation.view_model

import com.tnyx.features.splash.presentation.action.SplashAction
import com.tnyx.features.splash.presentation.state.SplashEffect
import com.tnyx.shared.auth.domain.model.AuthSession
import com.tnyx.shared.auth.domain.repository.AuthSessionProvider
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

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun signedOutSessionNavigatesToWelcome() = runTest {
        val viewModel = SplashViewModel(TestSessionProvider(null))
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
    fun persistedSessionNavigatesToMain() = runTest {
        val viewModel = SplashViewModel(TestSessionProvider(session()))
        val effects = mutableListOf<SplashEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleAction(SplashAction.Init)
        advanceUntilIdle()

        assertEquals(listOf(SplashEffect.NavigateToMain), effects)
        assertFalse(viewModel.uiState.value.isLoading)
        collectJob.cancel()
    }

    @Test
    fun repeatedInitEmitsOnlyOneNavigationEffect() = runTest {
        val viewModel = SplashViewModel(TestSessionProvider(session()))
        val effects = mutableListOf<SplashEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleAction(SplashAction.Init)
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
