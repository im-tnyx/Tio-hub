package com.tnyx.auth

import com.tnyx.shared.auth.domain.model.AuthSession
import com.tnyx.shared.auth.domain.repository.MutableAuthSessionStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Singleton
class SupabaseAuthSessionBridge @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val sessionStore: MutableAuthSessionStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch {
            supabaseClient.auth.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    syncAuthenticatedUser()
                }
            }
        }
    }

    private suspend fun syncAuthenticatedUser() {
        val user = supabaseClient.auth.currentUserOrNull() ?: return
        val email = user.email?.takeIf(String::isNotBlank) ?: return
        sessionStore.setSession(
            AuthSession(
                userId = user.id,
                email = email,
                displayName = email.substringBefore("@").replaceFirstChar(Char::titlecase),
                isDemo = false,
            ),
        )
    }
}
