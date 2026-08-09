package com.tnyx.features.workout.presentation.library.exerciseinfo

import androidx.lifecycle.SavedStateHandle
import com.tnyx.features.workout.domain.repository.ExerciseCatalogRepository
import com.tnyx.features.workout.presentation.MainDispatcherRule
import com.tnyx.shared.profile.domain.model.UserProfile
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule

class ExerciseInfoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun missingExerciseIdShowsSafeErrorState() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExerciseCatalogRepository()

        val viewModel = ExerciseInfoViewModel(
            savedStateHandle = SavedStateHandle(),
            catalogRepository = repository,
            profileRepository = FakeProfileRepository(),
        )

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Exercise details unavailable.", state.errorMessage)
        assertEquals(null, state.exercise)
    }

    @Test
    fun repositoryFailureDoesNotCrashAndShowsSafeErrorState() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExerciseCatalogRepository(
            exception = IllegalStateException("boom"),
        )

        val viewModel = ExerciseInfoViewModel(
            savedStateHandle = SavedStateHandle(mapOf("exerciseId" to "ex_deadlift")),
            catalogRepository = repository,
            profileRepository = FakeProfileRepository(),
        )

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Exercise details unavailable.", state.errorMessage)
        assertEquals(null, state.exercise)
    }

    private class FakeExerciseCatalogRepository(
        private val exception: Throwable? = null,
    ) : ExerciseCatalogRepository {
        override fun getExercises(): Flow<List<ExerciseDefinition>> = flowOf(emptyList())

        override fun searchExercises(query: String, muscleGroupFilter: String): Flow<List<ExerciseDefinition>> {
            return flowOf(emptyList())
        }

        override suspend fun getExerciseById(exerciseId: String): ExerciseDefinition? {
            exception?.let { throw it }
            return null
        }

        override suspend fun saveCustomExercise(
            exercise: ExerciseDefinition,
            mediaUpdate: com.tnyx.features.workout.domain.repository.CustomExerciseMediaUpdate,
        ) = Unit

        override suspend fun deleteCustomExercise(exerciseId: String) = Unit
    }

    private class FakeProfileRepository : ProfileRepository {
        private val profile = MutableStateFlow(
            UserProfile(
                id = "user-1",
                displayName = "Test User",
                dob = "2000-01-01",
                gender = "female",
                planLabel = "",
                weight = 0.0,
                height = 0,
                bmi = 0.0,
                bmr = 0,
            )
        )

        override fun getCurrentProfile(): Flow<UserProfile> = profile

        override fun getProfile(userId: String): Flow<UserProfile> = profile

        override suspend fun updateProfile(profile: UserProfile) = Unit

        override suspend fun updateAvatar(jpegBytes: ByteArray): String = ""

        override suspend fun removeAvatar() = Unit
    }
}
