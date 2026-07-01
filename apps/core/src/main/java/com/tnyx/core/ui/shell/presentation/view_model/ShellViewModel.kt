package com.tnyx.core.ui.shell.presentation.view_model

import androidx.lifecycle.ViewModel
import com.tnyx.core.ui.shell.presentation.action.ShellAction
import com.tnyx.core.ui.shell.presentation.state.ShellUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ShellViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ShellUiState())
    val uiState = _uiState.asStateFlow()

    fun handleAction(action: ShellAction) {
        when (action) {
            is ShellAction.TabSelected -> {
                _uiState.update { it.copy(selectedTab = action.tab) }
            }
            is ShellAction.ScrollChanged -> {
                val opacity = (action.offset / 100f).coerceIn(0f, 1f)
                _uiState.update { it.copy(appBarOpacity = opacity) }
            }
            ShellAction.PremiumClicked -> {
                // Navigate to premium screen.
            }
            ShellAction.StreakClicked -> {
                // Show streak details.
            }
            ShellAction.ProfileClicked -> {
                // Navigate to profile.
            }
            ShellAction.SettingsClicked -> {
                // Navigate to settings.
            }
        }
    }
}
