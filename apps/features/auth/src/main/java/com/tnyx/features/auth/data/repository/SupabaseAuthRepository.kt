package com.tnyx.features.auth.data.repository

import com.tnyx.features.auth.domain.model.AuthResult
import com.tnyx.features.auth.domain.model.DemoAccountConfig
import com.tnyx.features.auth.domain.repository.AuthRepository
import com.tnyx.features.auth.domain.repository.ExternalAuthGateway
import com.tnyx.shared.auth.domain.model.AuthSession
import com.tnyx.shared.auth.domain.repository.MutableAuthSessionStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Singleton
class SupabaseAuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val sessionStore: MutableAuthSessionStore,
    private val externalAuthGateway: ExternalAuthGateway,
    private val demoAccountConfig: DemoAccountConfig,
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): AuthResult {
        return runCatching {
            supabaseClient.auth.signInWith(Email) {
                this.email = email.trim()
                this.password = password
            }
            authenticatedFromCurrentUser()
        }.getOrElse { error ->
            AuthResult.Failure(error.message ?: "Could not sign in right now")
        }
    }

    override suspend fun signInWithGoogle(): AuthResult {
        return runCatching {
            externalAuthGateway.signInWithGoogle()
            AuthResult.ExternalAuthStarted
        }.getOrElse { error ->
            AuthResult.Failure(error.message ?: "Could not start Google sign-in right now")
        }
    }

    override suspend fun signInWithDemoAccount(): AuthResult {
        if (!demoAccountConfig.isConfigured) {
            return AuthResult.Failure("Demo account is not configured on this build")
        }
        return signIn(
            email = demoAccountConfig.normalizedEmail,
            password = demoAccountConfig.normalizedPassword,
        )
    }

    override suspend fun signUp(
        name: String,
        email: String,
        password: String,
    ): AuthResult {
        val normalizedEmail = email.trim()
        return runCatching {
            supabaseClient.auth.signUpWith(Email) {
                this.email = normalizedEmail
                this.password = password
                this.data = buildJsonObject {
                    put("display_name", JsonPrimitive(name.trim()))
                    put("full_name", JsonPrimitive(name.trim()))
                }
            }

            val user = supabaseClient.auth.currentUserOrNull()
            if (user == null) {
                AuthResult.VerificationRequired(normalizedEmail)
            } else {
                authenticatedFromCurrentUser()
            }
        }.getOrElse { error ->
            AuthResult.Failure(error.message ?: "Could not create your account right now")
        }
    }

    override suspend fun verifyOtp(email: String, code: String): AuthResult {
        val normalizedEmail = email.trim()
        return runCatching {
            supabaseClient.auth.verifyEmailOtp(
                OtpType.Email.SIGNUP,
                normalizedEmail,
                code.trim(),
            )
            authenticatedFromCurrentUser()
        }.getOrElse { error ->
            AuthResult.Failure(error.message ?: "Could not verify the code right now")
        }
    }

    override suspend fun resendOtp(email: String): AuthResult {
        val normalizedEmail = email.trim()
        return runCatching {
            supabaseClient.auth.resendEmail(
                OtpType.Email.SIGNUP,
                normalizedEmail,
                null,
            )
            AuthResult.VerificationRequired(normalizedEmail)
        }.getOrElse { error ->
            AuthResult.Failure(error.message ?: "Could not resend the code right now")
        }
    }

    override suspend fun restoreSessionIfAvailable(): AuthSession? {
        sessionStore.currentSession()?.let { return it }
        val user = supabaseClient.auth.currentUserOrNull() ?: return null
        val session = sessionFromUser(user.id, user.email, user.userMetadata?.get("display_name")?.toString()?.trim('"'))
        sessionStore.setSession(session)
        return session
    }

    override suspend fun signInAnonymously(): AuthResult {
        return runCatching {
            supabaseClient.auth.signInAnonymously()
            val user = supabaseClient.auth.currentUserOrNull()
                ?: return AuthResult.Failure("No authenticated Supabase user is available")
            val session = sessionFromUser(user.id, user.email, null)
            sessionStore.setSession(session)
            AuthResult.Authenticated(session)
        }.getOrElse { error ->
            AuthResult.Failure(error.message ?: "Could not start guest session right now")
        }
    }

    override suspend fun signOut() {
        runCatching { externalAuthGateway.signOut() }
        runCatching { supabaseClient.auth.clearSession() }
        sessionStore.clearSession()
    }

    private suspend fun authenticatedFromCurrentUser(): AuthResult {
        val user = supabaseClient.auth.currentUserOrNull()
            ?: return AuthResult.Failure("No authenticated Supabase user is available")
        val email = user.email?.takeIf(String::isNotBlank)
            ?: return AuthResult.Failure("Authenticated account is missing an email address")
        val session = sessionFromUser(
            userId = user.id,
            email = email,
            rawDisplayName = user.userMetadata
                ?.get("display_name")
                ?.toString()
                ?.trim('"'),
        ).copy(isDemo = false)
        sessionStore.setSession(session)
        return AuthResult.Authenticated(session)
    }

    private fun sessionFromUser(
        userId: String,
        email: String?,
        rawDisplayName: String?,
    ): AuthSession {
        val normalizedEmail = email?.takeIf(String::isNotBlank)
        val isAnonymous = normalizedEmail == null
        val fallbackEmail = normalizedEmail ?: "guest_${userId.take(8)}@tnyx.app"
        val displayName = rawDisplayName
            ?.takeIf(String::isNotBlank)
            ?: if (isAnonymous) {
                "Guest User"
            } else {
                fallbackEmail.substringBefore("@").replaceFirstChar(Char::titlecase)
            }
        return AuthSession(
            userId = userId,
            email = fallbackEmail,
            displayName = displayName,
            isDemo = isAnonymous,
        )
    }
}
