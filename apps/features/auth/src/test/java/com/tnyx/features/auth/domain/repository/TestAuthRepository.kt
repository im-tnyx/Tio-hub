package com.tnyx.features.auth.domain.repository

import com.tnyx.features.auth.domain.model.AuthResult

class TestAuthRepository : AuthRepository {
    var delayMs: Long = 0
    var signInResult: AuthResult = AuthResult.Failure("Not initialized")
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
        return signInResult
    }

    override suspend fun signInWithDemoAccount(): AuthResult {
        maybeDelay()
        return signInWithDemoResult
    }

    override suspend fun signUp(name: String, email: String, password: String): AuthResult {
        maybeDelay()
        return signUpResult
    }

    override suspend fun verifyOtp(email: String, code: String): AuthResult {
        maybeDelay()
        return verifyOtpResult
    }

    override suspend fun resendOtp(email: String): AuthResult {
        maybeDelay()
        return resendOtpResult
    }
}
