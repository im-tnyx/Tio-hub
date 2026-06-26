package com.tnyx.features.splash.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.tnyx.features.splash.presentation.action.SplashAction
import com.tnyx.features.splash.presentation.screen.SplashScreen
import com.tnyx.features.splash.presentation.state.SplashEffect
import com.tnyx.features.splash.presentation.view_model.SplashViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SplashRoute(
    onNavigateToWelcome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.handleAction(SplashAction.Init)
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SplashEffect.NavigateToWelcome -> onNavigateToWelcome()
            }
        }
    }

    SplashScreen(
        state = uiState,
        onAction = viewModel::handleAction
    )
}
