package com.tnyx.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tnyx.core.ui.shell.domain.model.DEFAULT_BOTTOM_NAV_TABS
import com.tnyx.core.ui.shell.domain.model.ShellTab
import com.tnyx.core.ui.shell.domain.model.normalizeBottomNavTabs
import com.tnyx.core.ui.shell.domain.repository.BottomNavPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "bottom_navigation"
private const val CURRENT_SCHEMA_VERSION = 1
private const val TAB_SEPARATOR = ","

private val Context.bottomNavPreferencesDataStore by preferencesDataStore(
    name = DATASTORE_NAME,
)

@Singleton
class DataStoreBottomNavPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : BottomNavPreferencesRepository {

    private val dataStore = context.bottomNavPreferencesDataStore

    override val tabs: Flow<List<ShellTab>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map(::decodeTabs)

    override suspend fun saveTabs(tabs: List<ShellTab>) {
        val normalizedTabs = normalizeBottomNavTabs(tabs)
        dataStore.edit { preferences ->
            preferences[TABS_KEY] = normalizedTabs.joinToString(TAB_SEPARATOR) { it.stableId }
            preferences[SCHEMA_VERSION_KEY] = CURRENT_SCHEMA_VERSION
        }
    }

    override suspend fun resetToDefault() {
        dataStore.edit { preferences ->
            preferences.remove(TABS_KEY)
            preferences.remove(SCHEMA_VERSION_KEY)
        }
    }

    private fun decodeTabs(preferences: Preferences): List<ShellTab> {
        val storedVersion = preferences[SCHEMA_VERSION_KEY] ?: CURRENT_SCHEMA_VERSION
        if (storedVersion > CURRENT_SCHEMA_VERSION) {
            return DEFAULT_BOTTOM_NAV_TABS
        }

        val rawTabs = preferences[TABS_KEY] ?: return DEFAULT_BOTTOM_NAV_TABS
        val storedIds = rawTabs
            .split(TAB_SEPARATOR)
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (storedIds.isEmpty()) {
            return DEFAULT_BOTTOM_NAV_TABS
        }

        val decodedTabs = storedIds.mapNotNull(ShellTab::fromStableId)
        if (decodedTabs.size != storedIds.size) {
            return DEFAULT_BOTTOM_NAV_TABS
        }

        return normalizeBottomNavTabs(decodedTabs)
    }

    private companion object {
        val TABS_KEY = stringPreferencesKey("ordered_tabs")
        val SCHEMA_VERSION_KEY = intPreferencesKey("schema_version")
    }
}
