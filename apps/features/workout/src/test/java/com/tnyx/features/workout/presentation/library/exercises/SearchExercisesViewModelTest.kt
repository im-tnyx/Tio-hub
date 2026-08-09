package com.tnyx.features.workout.presentation.library.exercises

import com.tnyx.features.workout.domain.repository.ExerciseCatalogRepository
import com.tnyx.features.workout.domain.repository.ExerciseViewPreferencesRepository
import com.tnyx.shared.profile.domain.model.UserProfile
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant
import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class SearchExercisesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeBenchPress = ExerciseDefinition(
        id = "ex_bench",
        name = "Bench Press (Barbell)",
        primaryMuscleGroups = listOf("chest"),
        equipment = listOf("barbell")
    )

    private val fakeTricepsPressdown = ExerciseDefinition(
        id = "ex_triceps",
        name = "Triceps Pressdown",
        primaryMuscleGroups = listOf("triceps"),
        equipment = listOf("machine")
    )

    private val fakeCustomExercise = ExerciseDefinition(
        id = "ex_custom",
        name = "My Custom Curl",
        primaryMuscleGroups = listOf("biceps"),
        equipment = listOf("dumbbell"),
        isCustom = true,
    )

    private val exercisesState = MutableStateFlow(listOf(fakeBenchPress, fakeTricepsPressdown, fakeCustomExercise))

    private val fakeRepository = object : ExerciseCatalogRepository {
        override fun getExercises(): Flow<List<ExerciseDefinition>> {
            return exercisesState
        }

        override fun searchExercises(query: String, muscleGroupFilter: String): Flow<List<ExerciseDefinition>> {
            return exercisesState.map { exercises ->
                exercises.filter { exercise ->
                    val matchesQuery = query.isBlank() || exercise.name.contains(query, ignoreCase = true)
                    val matchesMuscle = muscleGroupFilter.equals("ALL", ignoreCase = true) ||
                        exercise.primaryMuscleGroups.any { it.equals(muscleGroupFilter, ignoreCase = true) }
                    matchesQuery && matchesMuscle
                }
            }
        }

        override suspend fun getExerciseById(exerciseId: String): ExerciseDefinition? {
            return exercisesState.value.find { it.id == exerciseId }
        }

        override suspend fun saveCustomExercise(exercise: ExerciseDefinition) {}

        override suspend fun deleteCustomExercise(exerciseId: String) {
            exercisesState.value = exercisesState.value.filterNot { it.id == exerciseId }
        }
    }

    private val fakeViewPreferencesRepository = object : ExerciseViewPreferencesRepository {
        private val _viewType = MutableStateFlow(ExerciseViewType.LIST)
        override val viewType: Flow<ExerciseViewType> = _viewType

        override suspend fun saveViewType(viewType: ExerciseViewType) {
            _viewType.value = viewType
        }
    }

    private val fakeProfileRepository = object : ProfileRepository {
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

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStateLoadsCatalogExercises() = runTest {
        val viewModel = SearchExercisesViewModel(fakeRepository, fakeViewPreferencesRepository, fakeProfileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.exercises.size)
        assertEquals(ExerciseMediaVariant.FEMALE, state.mediaVariant)
    }

    @Test
    fun testQueryChangedFiltersExercises() = runTest {
        val viewModel = SearchExercisesViewModel(fakeRepository, fakeViewPreferencesRepository, fakeProfileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(SearchExercisesAction.QueryChanged("Bench"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.exercises.size)
        assertEquals("Bench Press (Barbell)", state.exercises.first().name)
    }

    @Test
    fun testFilterSelectedFiltersByMuscleGroup() = runTest {
        val viewModel = SearchExercisesViewModel(fakeRepository, fakeViewPreferencesRepository, fakeProfileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(SearchExercisesAction.FilterSelected("TRICEPS"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.exercises.size)
        assertEquals("Triceps Pressdown", state.exercises.first().name)
    }

    @Test
    fun testToggleViewTypePersistsChoice() = runTest {
        val viewModel = SearchExercisesViewModel(fakeRepository, fakeViewPreferencesRepository, fakeProfileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ExerciseViewType.LIST, viewModel.uiState.value.viewType)

        viewModel.onAction(SearchExercisesAction.ToggleViewType)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ExerciseViewType.GRID, viewModel.uiState.value.viewType)
    }

    @Test
    fun testDeleteCustomExerciseRemovesItFromState() = runTest {
        val viewModel = SearchExercisesViewModel(fakeRepository, fakeViewPreferencesRepository, fakeProfileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(SearchExercisesAction.ExerciseLongClicked("ex_custom"))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onAction(SearchExercisesAction.DeleteCustomExerciseClicked("ex_custom"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.exercises.size)
        assertEquals(null, state.selectedExerciseForActions)
    }
}
