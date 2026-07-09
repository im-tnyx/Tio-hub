package com.tnyx.features.settings.presentation.personal_info

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PersonalInfoRoute(
    onNavigateBack: () -> Unit,
    viewModel: PersonalInfoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    PersonalInfoScreen(
        state = uiState,
        onAction = { action ->
            when (action) {
                PersonalInfoAction.BackClicked -> onNavigateBack()
                else -> viewModel.handleAction(action)
            }
        }
    )
}
