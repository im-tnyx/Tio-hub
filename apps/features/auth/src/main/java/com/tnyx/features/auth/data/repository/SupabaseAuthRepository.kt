package com.tnyx.features.auth.data.repository

import com.tnyx.features.auth.domain.model.AuthResult
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
        return AuthResult.Failure("Demo account is disabled in sync mode")
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

    override suspend fun signInAnonymously(): AuthResult {
        return runCatching {
            supabaseClient.auth.signInAnonymously()
            val user = supabaseClient.auth.currentUserOrNull()
                ?: return AuthResult.Failure("No authenticated Supabase user is available")
            val email = user.email?.takeIf(String::isNotBlank) ?: "guest_${user.id.take(8)}@tnyx.app"
            val session = AuthSession(
                userId = user.id,
                email = email,
                displayName = "Guest User",
                isDemo = true,
            )
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
        val displayName = user.userMetadata
            ?.get("display_name")
            ?.toString()
            ?.trim('"')
            ?.takeIf(String::isNotBlank)
            ?: email.substringBefore("@").replaceFirstChar(Char::titlecase)

        val session = AuthSession(
            userId = user.id,
            email = email,
            displayName = displayName,
            isDemo = false,
        )
        sessionStore.setSession(session)
        return AuthResult.Authenticated(session)
    }
}
