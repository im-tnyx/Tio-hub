package com.tnyx.features.auth.domain.repository

import com.tnyx.features.auth.domain.model.AuthResult
import com.tnyx.shared.auth.domain.repository.MutableAuthSessionStore

class TestAuthRepository(
    private val sessionStore: MutableAuthSessionStore? = null,
) : AuthRepository {
    var delayMs: Long = 0
    var signInResult: AuthResult = AuthResult.Failure("Not initialized")
    var signInWithGoogleResult: AuthResult = AuthResult.Failure("Not initialized")
    var signInWithDemoResult: AuthResult = AuthResult.Failure("Not initialized")
    var signUpResult: AuthResult = AuthResult.Failure("Not initialized")
    var verifyOtpResult: AuthResult = AuthResult.Failure("Not initialized")
    var resendOtpResult: AuthResult = AuthResult.Failure("Not initialized")

    private suspend fun maybeDelay() {
        if (delayMs > 0) {
            kotlinx.coroutines.delay(delayMs)
        }
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        maybeDelay()
        val result = signInResult
        syncSession(result)
        return result
    }

    override suspend fun signInWithGoogle(): AuthResult {
        maybeDelay()
        val result = signInWithGoogleResult
        syncSession(result)
        return result
    }

    override suspend fun signInWithDemoAccount(): AuthResult {
        maybeDelay()
        val result = signInWithDemoResult
        syncSession(result)
        return result
    }

    override suspend fun signUp(name: String, email: String, password: String): AuthResult {
        maybeDelay()
        return signUpResult
    }

    override suspend fun verifyOtp(email: String, code: String): AuthResult {
        maybeDelay()
        val result = verifyOtpResult
        syncSession(result)
        return result
    }

    override suspend fun resendOtp(email: String): AuthResult {
        maybeDelay()
        return resendOtpResult
    }

    override suspend fun signOut() {
        sessionStore?.clearSession()
    }

    private suspend fun syncSession(result: AuthResult) {
        if (result is AuthResult.Authenticated) {
            sessionStore?.setSession(result.session)
        }
    }
}
