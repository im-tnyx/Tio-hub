package com.tnyx.data.onboarding

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.resume.ResumeManager as OnboardingResumeManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch

private const val RESUME_DATASTORE_NAME = "onboarding_resume_snapshot"

private val Context.onboardingResumeDataStore by preferencesDataStore(
    name = RESUME_DATASTORE_NAME,
)

@Singleton
class ResumeManager @Inject constructor(
    @ApplicationContext context: Context,
) : OnboardingResumeManager {
    private val dataStore = context.onboardingResumeDataStore

    override suspend fun restoreCheckpoint(): OnboardingCheckpoint? {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[RESUME_KEY]?.let(OnboardingCheckpointCodec::decodeOrNull)
            }
            .first()
    }

    override suspend fun saveCheckpoint(checkpoint: OnboardingCheckpoint) {
        dataStore.edit { preferences ->
            preferences[RESUME_KEY] = OnboardingCheckpointCodec.encode(checkpoint)
        }
    }

    override suspend fun clearCheckpoint() {
        dataStore.edit { preferences ->
            preferences.remove(RESUME_KEY)
        }
    }

    private companion object {
        val RESUME_KEY = stringPreferencesKey("resume_checkpoint_json")
    }
}
