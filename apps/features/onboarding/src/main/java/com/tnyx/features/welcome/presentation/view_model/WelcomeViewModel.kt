package com.tnyx.features.welcome.presentation.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.welcome.presentation.action.WelcomeAction
import com.tnyx.features.welcome.presentation.state.LegalDocumentType
import com.tnyx.features.welcome.presentation.state.WelcomeEffect
import com.tnyx.features.welcome.presentation.state.WelcomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<WelcomeEffect>()
    val effect = _effect.asSharedFlow()

    fun handleAction(action: WelcomeAction) {
        when (action) {
            WelcomeAction.GetStartedClicked -> {
                viewModelScope.launch { _effect.emit(WelcomeEffect.NavigateToHome) }
            }
            WelcomeAction.SignInClicked -> {
                viewModelScope.launch { _effect.emit(WelcomeEffect.NavigateToLogin) }
            }
            WelcomeAction.SkipForNowClicked -> {
                viewModelScope.launch { _effect.emit(WelcomeEffect.NavigateToHome) }
            }
            WelcomeAction.LanguageSelectorClicked -> {
                _uiState.update { it.copy(showLanguageSheet = true) }
            }
            WelcomeAction.LanguageSheetDismissed -> {
                _uiState.update { it.copy(showLanguageSheet = false) }
            }
            is WelcomeAction.LanguageChanged -> {
                _uiState.update { 
                    it.copy(
                        localeCode = action.localeCode,
                        showLanguageSheet = false
                    ) 
                }
            }
            is WelcomeAction.LegalDocumentClicked -> {
                val title = if (action.type == LegalDocumentType.TERMS_AND_CONDITIONS) {
                    _uiState.value.termsText
                } else {
                    _uiState.value.privacyText
                }
                
                val url = if (action.type == LegalDocumentType.TERMS_AND_CONDITIONS) {
                    "https://tnyx.com/terms"
                } else {
                    "https://tnyx.com/privacy"
                }

                viewModelScope.launch {
                    _effect.emit(WelcomeEffect.NavigateToLegal(title, url))
                }
            }
        }
    }
}
