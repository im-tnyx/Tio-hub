package com.tnyx.features.splash.presentation.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.splash.presentation.action.SplashAction
import com.tnyx.features.splash.presentation.state.SplashEffect
import com.tnyx.features.splash.presentation.state.SplashUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SplashEffect>()
    val effect = _effect.asSharedFlow()

    fun handleAction(action: SplashAction) {
        when (action) {
            is SplashAction.Init -> init()
        }
    }

    private fun init() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Auth check or data loading here
            delay(2000L)
            _effect.emit(SplashEffect.NavigateToWelcome)
        }
    }
}
