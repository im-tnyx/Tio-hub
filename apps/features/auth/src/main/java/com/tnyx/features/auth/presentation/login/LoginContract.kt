package com.tnyx.features.auth.presentation.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}

sealed interface LoginAction {
    data class EmailChanged(val value: String) : LoginAction
    data class PasswordChanged(val value: String) : LoginAction
    data object SignInClicked : LoginAction
    data object GoogleClicked : LoginAction
    data object DemoAccountClicked : LoginAction
    data object CreateAccountClicked : LoginAction
}

sealed interface LoginEffect {
    data object Authenticated : LoginEffect
    data object NavigateToSignup : LoginEffect
}
