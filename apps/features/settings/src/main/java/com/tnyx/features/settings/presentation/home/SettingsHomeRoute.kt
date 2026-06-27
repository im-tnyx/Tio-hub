package com.tnyx.features.settings.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsHomeRoute(
    onNavigateBack: () -> Unit,
    viewModel: SettingsHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsHomeScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                SettingsHomeAction.BackClicked -> onNavigateBack()
            }
        }
    )
}
