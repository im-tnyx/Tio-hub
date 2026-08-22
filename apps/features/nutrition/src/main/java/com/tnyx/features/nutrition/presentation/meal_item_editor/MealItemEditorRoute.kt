package com.tnyx.features.nutrition.presentation.meal_item_editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.tnyx.features.nutrition.domain.models.MealItem
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MealItemEditorRoute(
    onNavigateBack: () -> Unit,
    onItemSaved: (MealItem) -> Unit,
    onItemRemoved: (String) -> Unit,
    viewModel: MealItemEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                MealItemEditorEffect.NavigateBack -> onNavigateBack()
                is MealItemEditorEffect.ItemSaved -> onItemSaved(effect.item)
                is MealItemEditorEffect.ItemRemoved -> onItemRemoved(effect.itemId)
            }
        }
    }

    MealItemEditorScreen(
        state = uiState,
        onAction = viewModel::handleAction
    )
}
