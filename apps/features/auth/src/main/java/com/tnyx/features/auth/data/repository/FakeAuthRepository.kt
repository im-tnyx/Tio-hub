package com.tnyx.features.auth.data.repository

import com.tnyx.features.auth.domain.model.AuthResult
import com.tnyx.features.auth.domain.repository.AuthRepository
import com.tnyx.features.auth.domain.repository.ExternalAuthGateway
import com.tnyx.shared.auth.domain.model.AuthSession
import com.tnyx.shared.auth.domain.repository.MutableAuthSessionStore
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAuthRepository @Inject constructor(
    private val sessionStore: MutableAuthSessionStore,
    private val externalAuthGateway: ExternalAuthGateway,
) : AuthRepository {
    private val pendingDisplayNames = mutableMapOf<String, String>()
    private val knownDisplayNames = mutableMapOf<String, String>()

    constructor(
        sessionStore: MutableAuthSessionStore,
    ) : this(
        sessionStore = sessionStore,
        externalAuthGateway = NoOpExternalAuthGateway,
    )

    override suspend fun signIn(email: String, password: String): AuthResult {
        if (password.length < 6) {
            return AuthResult.Failure("Password must be at least 6 characters")
        }

        val normalizedEmail = normalizeEmail(email)
        return authenticated(
            AuthSession(
                userId = stableUserId(normalizedEmail),
                email = normalizedEmail,
                displayName = knownDisplayNames[normalizedEmail] ?: displayNameFrom(normalizedEmail),
                isDemo = false,
            ),
        )
    }

    override suspend fun signInWithDemoAccount(): AuthResult {
        return authenticated(
            AuthSession(
                userId = "demo-user",
                email = "demo@tnyx.app",
                displayName = "Demo User",
                isDemo = true,
            ),
        )
    }

    override suspend fun signInWithGoogle(): AuthResult {
        return runCatching {
            externalAuthGateway.signInWithGoogle()
            AuthResult.ExternalAuthStarted
        }.getOrElse { error ->
            AuthResult.Failure(
                error.message ?: "Could not start Google sign-in right now",
            )
        }
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

        val normalizedEmail = normalizeEmail(email)
        pendingDisplayNames[normalizedEmail] = name.trim()
        return AuthResult.VerificationRequired(email = normalizedEmail)
    }

    override suspend fun verifyOtp(email: String, code: String): AuthResult {
        if (code.length != 6) {
            return AuthResult.Failure("Enter the 6-digit code")
        }

        val normalizedEmail = normalizeEmail(email)
        val displayName = pendingDisplayNames.remove(normalizedEmail)
            ?: knownDisplayNames[normalizedEmail]
            ?: displayNameFrom(normalizedEmail)
        knownDisplayNames[normalizedEmail] = displayName

        return authenticated(
            AuthSession(
                userId = stableUserId(normalizedEmail),
                email = normalizedEmail,
                displayName = displayName,
                isDemo = false,
            ),
        )
    }

    override suspend fun resendOtp(email: String): AuthResult {
        return AuthResult.VerificationRequired(email = normalizeEmail(email))
    }

    override suspend fun restoreSessionIfAvailable(): AuthSession? {
        return sessionStore.currentSession()
    }

    override suspend fun signInAnonymously(): AuthResult {
        return authenticated(
            AuthSession(
                userId = "guest-user",
                email = "guest@tnyx.app",
                displayName = "Guest User",
                isDemo = true,
            ),
        )
    }

    override suspend fun signOut() {
        runCatching { externalAuthGateway.signOut() }
        sessionStore.clearSession()
    }

    private suspend fun authenticated(session: AuthSession): AuthResult.Authenticated {
        sessionStore.setSession(session)
        return AuthResult.Authenticated(session)
    }

    private fun normalizeEmail(email: String): String = email.trim().lowercase()

    private fun displayNameFrom(email: String): String {
        return email.substringBefore("@").replaceFirstChar(Char::titlecase)
    }

    private fun stableUserId(email: String): String {
        val namespacedEmail = "tio-fake-auth:$email"
        return UUID.nameUUIDFromBytes(
            namespacedEmail.toByteArray(StandardCharsets.UTF_8),
        ).toString()
    }
}

private object NoOpExternalAuthGateway : ExternalAuthGateway {
    override suspend fun signInWithGoogle() {
        error("Google sign-in is not configured in this test/runtime path")
    }

    override suspend fun signOut() = Unit
}
