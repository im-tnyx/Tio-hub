package com.tnyx.features.splash.presentation.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.splash.presentation.action.SplashAction
import com.tnyx.features.splash.presentation.state.SplashEffect
import com.tnyx.features.splash.presentation.state.SplashUiState
import com.tnyx.shared.auth.domain.repository.AuthSessionProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionProvider: AuthSessionProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SplashEffect>()
    val effect = _effect.asSharedFlow()
    private var initializationStarted = false

    fun handleAction(action: SplashAction) {
        when (action) {
            is SplashAction.Init -> init()
        }
    }

    private fun init() {
        if (initializationStarted) return
        initializationStarted = true

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(2000L)
            val session = sessionProvider.observeSession().first()
            _uiState.update { it.copy(isLoading = false) }
            _effect.emit(
                if (session == null) {
                    SplashEffect.NavigateToWelcome
                } else {
                    SplashEffect.NavigateToMain
                },
            )
        }
    }
}
