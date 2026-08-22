package com.tnyx.features.nutrition.presentation.meal_diary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MealDiaryRoute(
    onNavigateToMealDetail: (String) -> Unit,
    onNavigateToSearch: (LocalDate) -> Unit,
    onNavigateToMealCamera: (LocalDate) -> Unit,
    onShowOverview: (String) -> Unit,
    onNavigateToNutritionSettings: (() -> Unit)? = null,
    onNavigateToAppSettings: (() -> Unit)? = null,
    refreshSignal: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    viewModel: MealDiaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(refreshSignal) {
        if (refreshSignal) {
            viewModel.handleAction(MealDiaryAction.RefreshRequested)
            onRefreshConsumed()
        }
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MealDiaryEffect.NavigateToMealDetail -> onNavigateToMealDetail(effect.mealId)
                is MealDiaryEffect.NavigateToSearch -> onNavigateToSearch(effect.date)
                is MealDiaryEffect.NavigateToMealCamera -> onNavigateToMealCamera(effect.date)
                is MealDiaryEffect.ShowOverview -> onShowOverview(effect.target)
                MealDiaryEffect.NavigateToNutritionSettings -> onNavigateToNutritionSettings?.invoke()
                MealDiaryEffect.NavigateToAppSettings -> onNavigateToAppSettings?.invoke()
            }
        }
    }

    MealDiaryScreen(
        state = uiState,
        onAction = viewModel::handleAction
    )
}
