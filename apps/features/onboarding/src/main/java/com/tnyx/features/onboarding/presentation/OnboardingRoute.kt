package com.tnyx.features.onboarding.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OnboardingRoute(
    onCompleted: () -> Unit,
    onExit: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.handleAction(OnboardingAction.Init)
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                OnboardingEffect.Completed -> onCompleted()
                OnboardingEffect.Exit -> onExit()
            }
        }
    }

    OnboardingScreen(
        state = uiState,
        onAction = viewModel::handleAction,
    )
}
