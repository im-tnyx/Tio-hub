package com.tnyx.features.welcome.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.tnyx.features.welcome.presentation.screen.WelcomeScreen
import com.tnyx.features.welcome.presentation.state.WelcomeEffect
import com.tnyx.features.welcome.presentation.view_model.WelcomeViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun WelcomeRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToLegal: (title: String, url: String) -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                WelcomeEffect.NavigateToHome -> onNavigateToHome()
                WelcomeEffect.NavigateToLogin -> onNavigateToLogin()
                WelcomeEffect.ShowLanguageSelector -> { /* Handle language selector if needed via state or effect */ }
                is WelcomeEffect.NavigateToLegal -> onNavigateToLegal(effect.title, effect.url)
            }
        }
    }

    WelcomeScreen(
        state = uiState,
        onAction = viewModel::handleAction
    )
}
