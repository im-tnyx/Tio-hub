package com.tnyx.features.nutrition.presentation.targets

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun NutritionTargetsRoute(
    onNavigateBack: () -> Unit,
    viewModel: NutritionTargetsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                NutritionTargetsEffect.NavigateBack -> onNavigateBack()
                is NutritionTargetsEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    NutritionTargetsScreen(
        state = uiState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::handleAction
    )
}
