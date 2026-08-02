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

    private val _uiState = MutableStateFlow(
        MealSearchUiState(
            date = logDate,
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
                    state.copy(query = newQuery, searchResults = emptyList())
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
            MealSearchAction.BackClicked -> {
                viewModelScope.launch {
                    _effect.emit(MealSearchEffect.NavigateBack)
                }
            }
        }
    }
}
