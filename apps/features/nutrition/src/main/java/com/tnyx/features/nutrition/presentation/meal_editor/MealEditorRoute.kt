package com.tnyx.features.nutrition.presentation.meal_editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MealEditorRoute(
    onNavigateBack: () -> Unit,
    onNavigateToItemEditor: (String) -> Unit,
    viewModel: MealEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                MealEditorEffect.NavigateBack -> onNavigateBack()
                is MealEditorEffect.NavigateToItemEditor -> onNavigateToItemEditor(effect.itemId)
                MealEditorEffect.ShowShareOptions -> { /* Show share options */ }
                MealEditorEffect.ShowNameEditDialog -> { /* Show name edit dialog */ }
            }
        }
    }

    MealEditorScreen(
        state = uiState,
        onAction = viewModel::handleAction
    )
}
