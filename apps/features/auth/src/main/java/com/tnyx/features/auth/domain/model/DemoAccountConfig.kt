package com.tnyx.features.auth.domain.model

data class DemoAccountConfig(
    val email: String = "",
    val password: String = "",
) {
    val normalizedEmail: String
        get() = email.trim().lowercase()

    val normalizedPassword: String
        get() = password.trim()

    val isConfigured: Boolean
        get() = normalizedEmail.isNotBlank() && normalizedPassword.isNotBlank()
}
