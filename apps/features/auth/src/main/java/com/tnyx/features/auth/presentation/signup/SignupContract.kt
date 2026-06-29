package com.tnyx.features.auth.presentation.signup

data class SignupUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false
) {
    val canSubmit: Boolean
        get() = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && !isLoading
}

sealed interface SignupAction {
    data class NameChanged(val value: String) : SignupAction
    data class EmailChanged(val value: String) : SignupAction
    data class PasswordChanged(val value: String) : SignupAction
    data object SignupClicked : SignupAction
    data object LoginClicked : SignupAction
}

sealed interface SignupEffect {
    data class NavigateToOtp(val email: String) : SignupEffect
    data object NavigateToLogin : SignupEffect
    data object Authenticated : SignupEffect
}