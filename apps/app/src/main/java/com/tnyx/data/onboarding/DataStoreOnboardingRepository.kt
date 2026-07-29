package com.tnyx.data.onboarding

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.repository.OnboardingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private const val DATASTORE_NAME = "onboarding_checkpoint"

private val Context.onboardingCheckpointDataStore by preferencesDataStore(
    name = DATASTORE_NAME,
)

@Singleton
class DataStoreOnboardingRepository @Inject constructor(
    @ApplicationContext context: Context,
) : OnboardingRepository {
    private val dataStore = context.onboardingCheckpointDataStore

    override fun observeCheckpoint(): Flow<OnboardingCheckpoint?> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[CHECKPOINT_KEY]?.let(OnboardingCheckpointCodec::decodeOrNull)
            }
            .distinctUntilChanged()
    }

    override suspend fun saveCheckpoint(checkpoint: OnboardingCheckpoint) {
        dataStore.edit { preferences ->
            preferences[CHECKPOINT_KEY] = OnboardingCheckpointCodec.encode(checkpoint)
        }
    }

    override suspend fun clearCheckpoint() {
        dataStore.edit { preferences ->
            preferences.remove(CHECKPOINT_KEY)
        }
    }

    private companion object {
        val CHECKPOINT_KEY = stringPreferencesKey("checkpoint_json")
    }
}

internal object OnboardingCheckpointCodec {
    private val json = Json {
        classDiscriminator = "answer_type"
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun encode(checkpoint: OnboardingCheckpoint): String {
        return json.encodeToString(OnboardingCheckpoint.serializer(), checkpoint)
    }

    fun decodeOrNull(serializedCheckpoint: String): OnboardingCheckpoint? {
        return runCatching {
            json.decodeFromString(OnboardingCheckpoint.serializer(), serializedCheckpoint)
        }.getOrNull()
    }
}
