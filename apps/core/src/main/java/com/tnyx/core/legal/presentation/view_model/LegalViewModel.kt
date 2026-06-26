package com.tnyx.core.legal.presentation.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.core.legal.presentation.action.LegalAction
import com.tnyx.core.legal.presentation.state.LegalEffect
import com.tnyx.core.legal.presentation.state.LegalUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LegalViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LegalUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<LegalEffect>()
    val effect = _effect.asSharedFlow()

    fun init(title: String, url: String, isRemoteEnabled: Boolean) {
        _uiState.update { 
            it.copy(
                title = title,
                url = url,
                isRemoteEnabled = isRemoteEnabled,
                isLoading = isRemoteEnabled
            ) 
        }
    }

    fun handleAction(action: LegalAction) {
        when (action) {
            LegalAction.CloseTapped, LegalAction.BackdropTapped -> {
                viewModelScope.launch { _effect.emit(LegalEffect.Close) }
            }
            LegalAction.RetryTapped -> {
                _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    fun onLoadingFinished() {
        _uiState.update { it.copy(isLoading = false) }
    }
}
