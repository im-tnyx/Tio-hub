package com.tnyx.shared.auth.domain.model

data class AuthSession(
    val userId: String,
    val email: String,
    val displayName: String?,
    val isDemo: Boolean,
)
