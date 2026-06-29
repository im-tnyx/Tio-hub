package com.tnyx.features.auth.data.repository

import com.tnyx.features.auth.domain.model.AuthResult
import com.tnyx.features.auth.domain.model.AuthSession
import com.tnyx.features.auth.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {
    override suspend fun signIn(email: String, password: String): AuthResult {
        if (password.length < 6) {
            return AuthResult.Failure("Password must be at least 6 characters")
        }

        return AuthResult.Authenticated(
            session = AuthSession(
                userId = "fake-user-${email.hashCode()}",
                email = email,
                displayName = email.substringBefore("@").replaceFirstChar(Char::titlecase),
                isDemo = false
            )
        )
    }

    override suspend fun signInWithDemoAccount(): AuthResult {
        return AuthResult.Authenticated(
            session = AuthSession(
                userId = "demo-user",
                email = "demo@tnyx.app",
                displayName = "Demo User",
                isDemo = true
            )
        )
    }

    override suspend fun signUp(
        name: String,
        email: String,
        password: String
    ): AuthResult {
        if (name.isBlank()) {
            return AuthResult.Failure("Name is required")
        }
        if (password.length < 6) {
            return AuthResult.Failure("Password must be at least 6 characters")
        }

        return AuthResult.VerificationRequired(email = email)
    }

    override suspend fun verifyOtp(email: String, code: String): AuthResult {
        if (code.length != 6) {
            return AuthResult.Failure("Enter the 6-digit code")
        }

        return AuthResult.Authenticated(
            session = AuthSession(
                userId = "verified-user-${email.hashCode()}",
                email = email,
                displayName = email.substringBefore("@").replaceFirstChar(Char::titlecase),
                isDemo = false
            )
        )
    }

    override suspend fun resendOtp(email: String): AuthResult {
        return AuthResult.VerificationRequired(email = email)
    }
}
