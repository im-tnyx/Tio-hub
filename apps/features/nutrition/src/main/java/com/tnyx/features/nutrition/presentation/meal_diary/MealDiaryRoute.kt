package com.tnyx.features.nutrition.presentation.meal_diary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MealDiaryRoute(
    onNavigateToMealDetail: (String) -> Unit,
    onNavigateToAddMeal: () -> Unit,
    onShowOverview: (String) -> Unit,
    viewModel: MealDiaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MealDiaryEffect.NavigateToMealDetail -> onNavigateToMealDetail(effect.mealId)
                MealDiaryEffect.NavigateToAddMeal -> onNavigateToAddMeal()
                is MealDiaryEffect.ShowOverview -> onShowOverview(effect.target)
            }
        }
    }

    MealDiaryScreen(
        state = uiState,
        onAction = viewModel::handleAction
    )
}
