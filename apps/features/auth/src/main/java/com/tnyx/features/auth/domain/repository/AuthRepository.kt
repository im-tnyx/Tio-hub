package com.tnyx.features.auth.domain.repository

import com.tnyx.features.auth.domain.model.AuthResult

interface AuthRepository {
    suspend fun signIn(email: String, password: String): AuthResult

    suspend fun signInWithDemoAccount(): AuthResult

    suspend fun signUp(
        name: String,
        email: String,
        password: String
    ): AuthResult

    suspend fun verifyOtp(email: String, code: String): AuthResult

    suspend fun resendOtp(email: String): AuthResult
}
