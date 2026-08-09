package com.tnyx.features.nutrition.presentation.nutrition_overview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun NutritionOverviewRoute(
    onNavigateBack: () -> Unit,
    viewModel: NutritionOverviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                NutritionOverviewEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    NutritionOverviewScreen(
        state = uiState,
        onAction = viewModel::handleAction
    )
}
