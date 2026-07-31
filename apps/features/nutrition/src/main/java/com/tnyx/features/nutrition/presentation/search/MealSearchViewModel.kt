package com.tnyx.features.nutrition.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.navigation.NutritionScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MealSearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<NutritionScreen.MealSearch>()
    private val logDate: LocalDate = route.date
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: LocalDate.now()

    // Sample food catalog for search
    private val defaultCatalog = listOf(
        MealItem(id = "catalog-1", name = "Oatmeal with Berries", calories = 250, protein = 8.0, carbs = 45.0, fats = 4.0, quantity = 1.0, unit = "bowl"),
        MealItem(id = "catalog-2", name = "Grilled Chicken Salad", calories = 380, protein = 35.0, carbs = 12.0, fats = 18.0, quantity = 1.0, unit = "plate"),
        MealItem(id = "catalog-3", name = "Whole Wheat Roti", calories = 120, protein = 3.5, carbs = 22.0, fats = 2.0, quantity = 2.0, unit = "piece"),
        MealItem(id = "catalog-4", name = "Dal Tadka", calories = 180, protein = 9.0, carbs = 24.0, fats = 5.0, quantity = 1.0, unit = "bowl"),
        MealItem(id = "catalog-5", name = "Greek Yogurt Parfait", calories = 190, protein = 15.0, carbs = 20.0, fats = 3.0, quantity = 1.0, unit = "cup"),
        MealItem(id = "catalog-6", name = "Protein Shake (Whey)", calories = 150, protein = 25.0, carbs = 3.0, fats = 2.0, quantity = 1.0, unit = "scoop"),
        MealItem(id = "catalog-7", name = "Paneer Tikka", calories = 280, protein = 18.0, carbs = 8.0, fats = 20.0, quantity = 1.0, unit = "plate"),
        MealItem(id = "catalog-8", name = "Brown Rice & Egg Curry", calories = 420, protein = 22.0, carbs = 52.0, fats = 14.0, quantity = 1.0, unit = "serving"),
    )

    private val _uiState = MutableStateFlow(
        MealSearchUiState(
            date = logDate,
            searchResults = defaultCatalog,
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MealSearchEffect>()
    val effect = _effect.asSharedFlow()

    fun handleAction(action: MealSearchAction) {
        when (action) {
            is MealSearchAction.QueryChanged -> {
                val newQuery = action.query
                _uiState.update { state ->
                    val filtered = if (newQuery.isBlank()) {
                        defaultCatalog
                    } else {
                        defaultCatalog.filter { it.name.contains(newQuery, ignoreCase = true) }
                    }
                    state.copy(query = newQuery, searchResults = filtered)
                }
            }
            is MealSearchAction.CategorySelected -> {
                _uiState.update { it.copy(selectedCategory = action.category) }
            }
            is MealSearchAction.FoodItemSelected -> {
                viewModelScope.launch {
                    _effect.emit(MealSearchEffect.NavigateToMealEditor(logDate, action.item))
                }
            }
            MealSearchAction.CreateCustomMealClicked -> {
                viewModelScope.launch {
                    _effect.emit(MealSearchEffect.NavigateToMealEditor(logDate, null))
                }
            }
            MealSearchAction.BackClicked -> {
                viewModelScope.launch {
                    _effect.emit(MealSearchEffect.NavigateBack)
                }
            }
        }
    }
}
