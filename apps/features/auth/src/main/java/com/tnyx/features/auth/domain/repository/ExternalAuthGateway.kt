package com.tnyx.features.auth.domain.repository

interface ExternalAuthGateway {
    suspend fun signInWithGoogle()

    suspend fun signOut()
}
