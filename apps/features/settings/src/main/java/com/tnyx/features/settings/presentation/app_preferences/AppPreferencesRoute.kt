package com.tnyx.features.settings.presentation.app_preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AppPreferencesRoute(
    onNavigateBack: () -> Unit,
    viewModel: AppPreferencesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AppPreferencesScreen(
        state = uiState,
        onAction = { action ->
            when (action) {
                AppPreferencesAction.BackClicked -> onNavigateBack()
                else -> viewModel.handleAction(action)
            }
        }
    )
}
