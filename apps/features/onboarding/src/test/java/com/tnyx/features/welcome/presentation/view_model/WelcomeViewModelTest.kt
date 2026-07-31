package com.tnyx.features.welcome.presentation.view_model

import com.tnyx.features.onboarding.presentation.MainDispatcherRule
import com.tnyx.features.welcome.presentation.action.WelcomeAction
import com.tnyx.features.welcome.presentation.state.WelcomeEffect
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
        )
    }

    @Test
    fun skipNavigatesDirectlyToMain() = runTest {
        assertEffect(
            action = WelcomeAction.SkipForNowClicked,
            expected = WelcomeEffect.NavigateToMain,
        )
    }

    @Test
    fun signInNavigatesToLogin() = runTest {
        assertEffect(
            action = WelcomeAction.SignInClicked,
            expected = WelcomeEffect.NavigateToLogin,
        )
    }

    private suspend fun kotlinx.coroutines.test.TestScope.assertEffect(
        action: WelcomeAction,
        expected: WelcomeEffect,
    ) {
        val viewModel = WelcomeViewModel()
        val effects = mutableListOf<WelcomeEffect>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleAction(action)
        advanceUntilIdle()

        assertEquals(listOf(expected), effects)
        collectJob.cancel()
    }
}
