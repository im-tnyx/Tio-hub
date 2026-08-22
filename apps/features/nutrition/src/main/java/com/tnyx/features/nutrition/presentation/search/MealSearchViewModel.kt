package com.tnyx.features.nutrition.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.nutrition.domain.repository.FoodSearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MealSearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val foodSearchRepository: FoodSearchRepository,
) : ViewModel() {

    private val logDate: LocalDate = savedStateHandle.get<String>("date")
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: LocalDate.now()
    private val initialQuery = savedStateHandle.get<String>("initialQuery").orEmpty().trim()
    private var searchJob: Job? = null

    private val _uiState = MutableStateFlow(
        MealSearchUiState(query = initialQuery, date = logDate),
    )
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MealSearchEffect>()
    val effect = _effect.asSharedFlow()

    init {
        if (initialQuery.length >= MIN_QUERY_LENGTH) {
            scheduleSearch(initialQuery, debounceMillis = 0L)
        }
    }

    fun handleAction(action: MealSearchAction) {
        when (action) {
            is MealSearchAction.QueryChanged -> {
                searchJob?.cancel()
                _uiState.update {
                    it.copy(
                        query = action.query,
                        searchResults = emptyList(),
                        isSearching = false,
                        hasSearched = false,
                        errorMessage = null,
                        selectedGroup = null,
                    )
                }
                scheduleSearch(action.query)
            }
            MealSearchAction.SearchSubmitted -> scheduleSearch(
                query = _uiState.value.query,
                debounceMillis = 0L,
            )
            is MealSearchAction.GroupSelected -> {
                _uiState.update { it.copy(selectedGroup = action.group) }
            }
            MealSearchAction.GroupClosed -> {
                _uiState.update { it.copy(selectedGroup = null) }
            }
            is MealSearchAction.FoodItemSelected -> {
                viewModelScope.launch {
                    _effect.emit(MealSearchEffect.NavigateToMealEditor(logDate, action.item))
                }
            }
            MealSearchAction.BackClicked -> {
                if (_uiState.value.selectedGroup != null) {
                    _uiState.update { it.copy(selectedGroup = null) }
                } else {
                    viewModelScope.launch { _effect.emit(MealSearchEffect.NavigateBack) }
                }
            }
        }
    }

    private fun scheduleSearch(
        query: String,
        debounceMillis: Long = SEARCH_DEBOUNCE_MILLIS,
    ) {
        val normalizedQuery = query.trim()
        searchJob?.cancel()
        if (normalizedQuery.length < MIN_QUERY_LENGTH) {
            return
        }

        searchJob = viewModelScope.launch {
            if (debounceMillis > 0L) delay(debounceMillis)
            _uiState.update {
                it.copy(isSearching = true, hasSearched = false, errorMessage = null)
            }
            try {
                val results = foodSearchRepository.search(normalizedQuery)
                _uiState.update {
                    it.copy(
                        searchResults = results,
                        isSearching = false,
                        hasSearched = true,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                _uiState.update {
                    it.copy(
                        searchResults = emptyList(),
                        isSearching = false,
                        hasSearched = true,
                        errorMessage = "Food search is unavailable. Check your connection and try again.",
                    )
                }
            }
        }
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 2
        const val SEARCH_DEBOUNCE_MILLIS = 700L
    }
}
