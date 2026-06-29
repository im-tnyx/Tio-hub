package com.tnyx.features.auth.domain.model

sealed interface AuthResult {
    data class Authenticated(val session: AuthSession) : AuthResult
    data class VerificationRequired(val email: String) : AuthResult
    data class Failure(val message: String) : AuthResult
}
