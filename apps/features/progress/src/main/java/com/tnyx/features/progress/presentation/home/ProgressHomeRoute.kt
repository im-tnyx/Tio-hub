package com.tnyx.features.progress.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProgressHomeRoute(
    viewModel: ProgressHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProgressHomeScreen(
        uiState = uiState,
        onAction = { /* Navigation will be owned by ProgressNavGraph when screens are implemented. */ }
    )
}
