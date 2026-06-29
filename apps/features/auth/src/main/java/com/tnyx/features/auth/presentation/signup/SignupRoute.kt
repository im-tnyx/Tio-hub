package com.tnyx.features.auth.presentation.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignupRoute(
    onNavigateToLogin: () -> Unit,
    onNavigateToOtp: (String) -> Unit,
    onAuthSuccess: () -> Unit,
    viewModel: SignupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                SignupEffect.NavigateToLogin -> onNavigateToLogin()
                is SignupEffect.NavigateToOtp -> onNavigateToOtp(effect.email)
                SignupEffect.Authenticated -> onAuthSuccess()
            }
        }
    }

    SignupScreen(
        state = uiState,
        onAction = viewModel::handleAction
    )
}