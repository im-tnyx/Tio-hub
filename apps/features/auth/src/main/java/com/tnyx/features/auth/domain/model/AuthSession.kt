package com.tnyx.features.auth.domain.model

data class AuthSession(
    val userId: String,
    val email: String,
    val displayName: String?,
    val isDemo: Boolean
)
