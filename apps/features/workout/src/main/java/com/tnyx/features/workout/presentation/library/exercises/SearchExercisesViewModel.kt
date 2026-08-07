package com.tnyx.features.workout.presentation.library.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.workout.domain.repository.ExerciseCatalogRepository
import com.tnyx.features.workout.domain.repository.ExerciseViewPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchExercisesViewModel @Inject constructor(
    private val catalogRepository: ExerciseCatalogRepository,
    private val viewPreferencesRepository: ExerciseViewPreferencesRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow("ALL")
    private val isSearchActive = MutableStateFlow(false)

    private val debouncedSearchQuery = searchQuery
        .debounce { query -> if (query.isEmpty()) 0L else 300L }

    private val _uiState = MutableStateFlow(SearchExercisesUiState(isLoading = true))
    val uiState: StateFlow<SearchExercisesUiState> = _uiState.asStateFlow()

    init {
        viewPreferencesRepository.viewType.onEach { savedViewType ->
            _uiState.update { it.copy(viewType = savedViewType) }
        }.launchIn(viewModelScope)

        combine(debouncedSearchQuery, selectedFilter) { query, filter ->
            Pair(query, filter)
        }.flatMapLatest { (query, filter) ->
            catalogRepository.searchExercises(query, filter)
        }.onEach { exerciseList ->
            _uiState.update { state ->
                state.copy(
                    searchQuery = searchQuery.value,
                    selectedFilter = selectedFilter.value,
                    isSearchActive = isSearchActive.value,
                    exercises = exerciseList,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: SearchExercisesAction) {
        when (action) {
            is SearchExercisesAction.QueryChanged -> {
                searchQuery.value = action.query
                _uiState.update { it.copy(searchQuery = action.query) }
            }
            is SearchExercisesAction.FilterSelected -> {
                selectedFilter.value = action.filter
                _uiState.update { it.copy(selectedFilter = action.filter) }
            }
            SearchExercisesAction.SearchIconClicked -> {
                isSearchActive.value = true
                _uiState.update { it.copy(isSearchActive = true) }
            }
            SearchExercisesAction.SearchModeDismissed -> {
                isSearchActive.value = false
                searchQuery.value = ""
                _uiState.update { it.copy(isSearchActive = false, searchQuery = "") }
            }
            SearchExercisesAction.BackClicked,
            SearchExercisesAction.FilterIconClicked,
            SearchExercisesAction.CreateIconClicked,
            is SearchExercisesAction.ExerciseInfoClicked,
            is SearchExercisesAction.ExerciseSelected -> {
                // Handled in Route or consumer host
            }
            is SearchExercisesAction.ExerciseLongClicked -> {
                val targetExercise = _uiState.value.exercises.find { it.id == action.exerciseId }
                _uiState.update { it.copy(selectedExerciseForActions = targetExercise) }
            }
            SearchExercisesAction.ExerciseActionsDismissed -> {
                _uiState.update { it.copy(selectedExerciseForActions = null) }
            }
            SearchExercisesAction.ToggleViewType -> {
                val nextViewType = if (_uiState.value.viewType == ExerciseViewType.LIST) ExerciseViewType.GRID else ExerciseViewType.LIST
                _uiState.update { it.copy(viewType = nextViewType) }
                viewModelScope.launch {
                    viewPreferencesRepository.saveViewType(nextViewType)
                }
            }
        }
    }
}
