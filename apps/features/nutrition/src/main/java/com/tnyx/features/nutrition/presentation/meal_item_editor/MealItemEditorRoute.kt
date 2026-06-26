package com.tnyx.features.nutrition.presentation.meal_item_editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MealItemEditorRoute(
    onNavigateBack: () -> Unit,
    viewModel: MealItemEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                MealItemEditorEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    MealItemEditorScreen(
        state = uiState,
        onAction = viewModel::handleAction
    )
}
