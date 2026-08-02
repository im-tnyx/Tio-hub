package com.tnyx.features.welcome.presentation.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.auth.domain.model.AuthResult
import com.tnyx.features.auth.domain.repository.AuthRepository
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
class WelcomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<WelcomeEffect>()
    val effect = _effect.asSharedFlow()

    fun handleAction(action: WelcomeAction) {
        when (action) {
            WelcomeAction.GetStartedClicked -> {
                viewModelScope.launch { _effect.emit(WelcomeEffect.NavigateToOnboarding) }
            }
            WelcomeAction.SignInClicked -> {
                viewModelScope.launch { _effect.emit(WelcomeEffect.NavigateToLogin) }
            }
            WelcomeAction.SkipForNowClicked -> {
                signInWithDemoAccount()
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
                        showLanguageSheet = false,
                        skipError = null,
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

    private fun signInWithDemoAccount() {
        if (_uiState.value.isSkipLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSkipLoading = true, skipError = null) }
            when (val result = authRepository.signInWithDemoAccount()) {
                is AuthResult.Authenticated -> _effect.emit(WelcomeEffect.NavigateToMain)
                is AuthResult.Failure -> _uiState.update { it.copy(skipError = result.message) }
                AuthResult.ExternalAuthStarted -> _uiState.update {
                    it.copy(skipError = "Demo account sign-in needs a direct session, not browser auth")
                }
                is AuthResult.VerificationRequired -> _uiState.update {
                    it.copy(skipError = "Demo account should not require verification")
                }
            }
            _uiState.update { it.copy(isSkipLoading = false) }
        }
    }
}
