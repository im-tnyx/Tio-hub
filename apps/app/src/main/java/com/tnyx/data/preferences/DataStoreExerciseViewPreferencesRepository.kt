package com.tnyx.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tnyx.features.workout.domain.repository.ExerciseViewPreferencesRepository
import com.tnyx.features.workout.presentation.library.exercises.ExerciseViewType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "exercise_view_preferences"

private val Context.exerciseViewDataStore by preferencesDataStore(
    name = DATASTORE_NAME,
)

@Singleton
class DataStoreExerciseViewPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : ExerciseViewPreferencesRepository {

    private val dataStore = context.exerciseViewDataStore

    override val viewType: Flow<ExerciseViewType> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val storedName = preferences[VIEW_TYPE_KEY]
            if (storedName != null) {
                try {
                    ExerciseViewType.valueOf(storedName)
                } catch (_: Exception) {
                    ExerciseViewType.LIST
                }
            } else {
                ExerciseViewType.LIST
            }
        }

    override suspend fun saveViewType(viewType: ExerciseViewType) {
        dataStore.edit { preferences ->
            preferences[VIEW_TYPE_KEY] = viewType.name
        }
    }

    private companion object {
        val VIEW_TYPE_KEY = stringPreferencesKey("exercise_view_type")
    }
}
