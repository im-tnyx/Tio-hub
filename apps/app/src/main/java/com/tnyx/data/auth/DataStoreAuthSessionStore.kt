package com.tnyx.data.auth

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tnyx.shared.auth.domain.model.AuthSession
import com.tnyx.shared.auth.domain.repository.MutableAuthSessionStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

private const val DATASTORE_NAME = "auth_session"

private val Context.authSessionDataStore by preferencesDataStore(
    name = DATASTORE_NAME,
)

@Singleton
class DataStoreAuthSessionStore @Inject constructor(
    @ApplicationContext context: Context,
) : MutableAuthSessionStore {
    private val dataStore = context.authSessionDataStore

    @Volatile
    private var cachedSession: AuthSession? = null

    override fun observeSession(): Flow<AuthSession?> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map(::decodeSession)
            .distinctUntilChanged()
            .onEach { session -> cachedSession = session }
    }

    override fun currentSession(): AuthSession? = cachedSession

    override suspend fun setSession(session: AuthSession) {
        require(session.userId.isNotBlank()) { "Auth session user ID is required" }
        require(session.email.isNotBlank()) { "Auth session email is required" }

        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = session.userId
            preferences[EMAIL_KEY] = session.email
            preferences[IS_DEMO_KEY] = session.isDemo
            session.displayName
                ?.takeIf(String::isNotBlank)
                ?.let { displayName -> preferences[DISPLAY_NAME_KEY] = displayName }
                ?: preferences.remove(DISPLAY_NAME_KEY)
        }
        cachedSession = session
    }

    override suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(EMAIL_KEY)
            preferences.remove(DISPLAY_NAME_KEY)
            preferences.remove(IS_DEMO_KEY)
        }
        cachedSession = null
    }

    private fun decodeSession(preferences: Preferences): AuthSession? {
        val userId = preferences[USER_ID_KEY]?.takeIf(String::isNotBlank) ?: return null
        val email = preferences[EMAIL_KEY]?.takeIf(String::isNotBlank) ?: return null
        return AuthSession(
            userId = userId,
            email = email,
            displayName = preferences[DISPLAY_NAME_KEY]?.takeIf(String::isNotBlank),
            isDemo = preferences[IS_DEMO_KEY] ?: false,
        )
    }

    private companion object {
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val EMAIL_KEY = stringPreferencesKey("email")
        val DISPLAY_NAME_KEY = stringPreferencesKey("display_name")
        val IS_DEMO_KEY = booleanPreferencesKey("is_demo")
    }
}
