package com.tnyx.features.settings.presentation.bottom_navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.core.ui.shell.domain.model.DEFAULT_BOTTOM_NAV_TABS
import com.tnyx.core.ui.shell.domain.model.normalizeBottomNavTabs
import com.tnyx.core.ui.shell.domain.repository.BottomNavPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BottomNavigationViewModel @Inject constructor(
    private val repository: BottomNavPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BottomNavigationUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<BottomNavigationEffect>()
    val effect = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            runCatching { repository.tabs.first() }
                .onSuccess { tabs ->
                    val normalizedTabs = normalizeBottomNavTabs(tabs)
                    _uiState.value = BottomNavigationUiState(
                        savedTabs = normalizedTabs,
                        draftTabs = normalizedTabs,
                        isLoading = false,
                    )
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load navigation settings.",
                        )
                    }
                }
        }
    }

    fun handleAction(action: BottomNavigationAction) {
        when (action) {
            BottomNavigationAction.BackClicked -> Unit
            is BottomNavigationAction.ToggleTab -> updateDraft(
                toggleBottomNavigationTab(_uiState.value.draftTabs, action.tab)
            )
            is BottomNavigationAction.MoveTabUp -> updateDraft(
                moveBottomNavigationTab(_uiState.value.draftTabs, action.tab, offset = -1)
            )
            is BottomNavigationAction.MoveTabDown -> updateDraft(
                moveBottomNavigationTab(_uiState.value.draftTabs, action.tab, offset = 1)
            )
            BottomNavigationAction.ResetClicked -> updateDraft(DEFAULT_BOTTOM_NAV_TABS)
            BottomNavigationAction.SaveClicked -> save()
            BottomNavigationAction.DismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun updateDraft(tabs: List<com.tnyx.core.ui.shell.domain.model.ShellTab>) {
        _uiState.update { state ->
            if (state.isSaving) state else state.copy(draftTabs = tabs, errorMessage = null)
        }
    }

    private fun save() {
        val state = _uiState.value
        if (!state.canSave) return

        val tabsToSave = normalizeBottomNavTabs(state.draftTabs)
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            runCatching { repository.saveTabs(tabsToSave) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            savedTabs = tabsToSave,
                            draftTabs = tabsToSave,
                            isSaving = false,
                        )
                    }
                    _effect.emit(BottomNavigationEffect.Saved)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: "Unable to save navigation settings.",
                        )
                    }
                }
        }
    }
}
