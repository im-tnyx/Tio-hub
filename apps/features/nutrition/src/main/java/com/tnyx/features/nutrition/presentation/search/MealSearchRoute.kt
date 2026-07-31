package com.tnyx.features.nutrition.presentation.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.tnyx.features.nutrition.domain.models.MealItem
import java.time.LocalDate
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MealSearchRoute(
    onNavigateBack: () -> Unit,
    onNavigateToMealEditor: (LocalDate, MealItem?) -> Unit,
    viewModel: MealSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                MealSearchEffect.NavigateBack -> onNavigateBack()
                is MealSearchEffect.NavigateToMealEditor -> onNavigateToMealEditor(effect.date, effect.initialItem)
            }
        }
    }

    MealSearchScreen(
        state = uiState,
        onAction = viewModel::handleAction,
    )
}
