package com.tnyx.features.settings.presentation.bottom_navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BottomNavigationRoute(
    onNavigateBack: () -> Unit,
    viewModel: BottomNavigationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                BottomNavigationEffect.Saved -> onNavigateBack()
            }
        }
    }

    BottomNavigationScreen(
        state = uiState,
        onAction = { action ->
            when (action) {
                BottomNavigationAction.BackClicked -> onNavigateBack()
                else -> viewModel.handleAction(action)
            }
        },
    )
}
