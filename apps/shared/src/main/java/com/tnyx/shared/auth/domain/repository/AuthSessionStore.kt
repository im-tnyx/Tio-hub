package com.tnyx.shared.auth.domain.repository

import com.tnyx.shared.auth.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

interface AuthSessionProvider {
    fun observeSession(): Flow<AuthSession?>
    fun currentSession(): AuthSession?
}

interface MutableAuthSessionStore : AuthSessionProvider {
    suspend fun setSession(session: AuthSession)
    suspend fun clearSession()
}
