package com.tnyx.auth

import com.tnyx.features.auth.domain.repository.ExternalAuthGateway
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseExternalAuthGateway @Inject constructor(
    private val supabaseClient: SupabaseClient,
) : ExternalAuthGateway {
    override suspend fun signInWithGoogle() {
        supabaseClient.auth.signInWith(Google)
    }

    override suspend fun signOut() {
        supabaseClient.auth.signOut()
    }
}
