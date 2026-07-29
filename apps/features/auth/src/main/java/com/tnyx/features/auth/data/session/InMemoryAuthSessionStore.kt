package com.tnyx.features.auth.data.session

import com.tnyx.shared.auth.domain.model.AuthSession
import com.tnyx.shared.auth.domain.repository.MutableAuthSessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAuthSessionStore : MutableAuthSessionStore {
    private val activeSession = MutableStateFlow<AuthSession?>(null)

    override fun observeSession(): Flow<AuthSession?> = activeSession.asStateFlow()

    override fun currentSession(): AuthSession? = activeSession.value

    override suspend fun setSession(session: AuthSession) {
        activeSession.value = session
    }

    override suspend fun clearSession() {
        activeSession.value = null
    }
}
