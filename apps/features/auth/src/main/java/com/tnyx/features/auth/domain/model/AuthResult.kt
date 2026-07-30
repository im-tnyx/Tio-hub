package com.tnyx.features.auth.domain.model

import com.tnyx.shared.auth.domain.model.AuthSession

sealed interface AuthResult {
    data class Authenticated(val session: AuthSession) : AuthResult
    data object ExternalAuthStarted : AuthResult
    data class VerificationRequired(val email: String) : AuthResult
    data class Failure(val message: String) : AuthResult
}
