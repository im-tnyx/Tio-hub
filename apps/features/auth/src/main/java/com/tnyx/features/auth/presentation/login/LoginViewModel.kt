package com.tnyx.features.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.auth.domain.model.AuthResult
import com.tnyx.features.auth.domain.repository.AuthRepository
import com.tnyx.shared.auth.domain.repository.AuthSessionProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    sessionProvider: AuthSessionProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            sessionProvider.observeSession()
                .filterNotNull()
                .collect {
                    _effect.emit(LoginEffect.Authenticated)
                }
        }
    }

    fun handleAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged -> _uiState.update {
                it.copy(email = action.value, emailError = null)
            }
            is LoginAction.PasswordChanged -> _uiState.update {
                it.copy(password = action.value, passwordError = null)
            }
            LoginAction.SignInClicked -> submit()
            LoginAction.GoogleClicked -> signInWithGoogle()
            LoginAction.DemoAccountClicked -> signInWithDemoAccount()
            LoginAction.CreateAccountClicked -> emitEffect(LoginEffect.NavigateToSignup)
        }
    }

    private fun submit() {
        val state = _uiState.value
        val email = state.email.trim()
        val password = state.password
        val emailError = when {
            email.isBlank() -> "Email is required"
            "@" !in email -> "Enter a valid email"
            else -> null
        }
        val passwordError = when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }

        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    email = email,
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(email = email, isLoading = true) }
            when (val result = authRepository.signIn(email = email, password = password)) {
                is AuthResult.Authenticated -> _uiState.update {
                    it.copy(emailError = null, passwordError = null)
                }
                AuthResult.ExternalAuthStarted -> Unit
                is AuthResult.Failure -> _uiState.update {
                    it.copy(passwordError = result.message)
                }
                is AuthResult.VerificationRequired -> _uiState.update {
                    it.copy(emailError = "Verify your email before signing in")
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, emailError = null, passwordError = null) }
            when (val result = authRepository.signInWithGoogle()) {
                AuthResult.ExternalAuthStarted -> Unit
                is AuthResult.Authenticated -> _uiState.update {
                    it.copy(emailError = null, passwordError = null)
                }
                is AuthResult.Failure -> _uiState.update {
                    it.copy(emailError = result.message)
                }
                is AuthResult.VerificationRequired -> _uiState.update {
                    it.copy(emailError = "Complete Google sign-in in the opened browser")
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun signInWithDemoAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, emailError = null, passwordError = null) }
            when (val result = authRepository.signInWithDemoAccount()) {
                is AuthResult.Authenticated -> _uiState.update {
                    it.copy(emailError = null, passwordError = null)
                }
                AuthResult.ExternalAuthStarted -> Unit
                is AuthResult.Failure -> _uiState.update {
                    it.copy(passwordError = result.message)
                }
                is AuthResult.VerificationRequired -> _uiState.update {
                    it.copy(emailError = "Demo account requires verification")
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun emitEffect(effect: LoginEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
