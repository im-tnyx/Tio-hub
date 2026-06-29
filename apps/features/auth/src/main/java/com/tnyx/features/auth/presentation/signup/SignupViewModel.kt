package com.tnyx.features.auth.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.auth.domain.model.AuthResult
import com.tnyx.features.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SignupEffect>()
    val effect = _effect.asSharedFlow()

    fun handleAction(action: SignupAction) {
        when (action) {
            is SignupAction.NameChanged -> _uiState.update {
                it.copy(name = action.value, nameError = null)
            }
            is SignupAction.EmailChanged -> _uiState.update {
                it.copy(email = action.value, emailError = null)
            }
            is SignupAction.PasswordChanged -> _uiState.update {
                it.copy(password = action.value, passwordError = null)
            }
            SignupAction.SignupClicked -> submit()
            SignupAction.LoginClicked -> emitEffect(SignupEffect.NavigateToLogin)
        }
    }

    private fun submit() {
        val state = _uiState.value
        val name = state.name.trim()
        val email = state.email.trim()
        val password = state.password
        val nameError = if (name.isBlank()) "Name is required" else null
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

        if (nameError != null || emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    name = name,
                    email = email,
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(name = name, email = email, isLoading = true) }
            when (val result = authRepository.signUp(name = name, email = email, password = password)) {
                is AuthResult.Authenticated -> _effect.emit(SignupEffect.Authenticated)
                is AuthResult.VerificationRequired -> _effect.emit(SignupEffect.NavigateToOtp(result.email))
                is AuthResult.Failure -> _uiState.update {
                    it.copy(passwordError = result.message)
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun emitEffect(effect: SignupEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}