package com.tnyx.features.workout.presentation.library.exerciseinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tnyx.features.workout.domain.repository.ExerciseCatalogRepository
import com.tnyx.features.workout.navigation.WorkoutDestination
import com.tnyx.features.workout.presentation.library.toExerciseMediaVariant
import com.tnyx.shared.profile.domain.model.UserProfile
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ExerciseInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogRepository: ExerciseCatalogRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val exerciseId = runCatching {
        savedStateHandle.toRoute<WorkoutDestination.ExerciseInfo>().exerciseId
    }.getOrNull()
        ?: savedStateHandle.get<String>(EXERCISE_ID_KEY)
        ?: savedStateHandle.get<String>("id")

    private val _uiState = MutableStateFlow(ExerciseInfoUiState())
    val uiState: StateFlow<ExerciseInfoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (exerciseId.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Exercise details unavailable.",
                    )
                }
                return@launch
            }

            // Fetch profile upfront so gender mediaVariant is known on the VERY FIRST render pass
            val profile = runCatching {
                profileRepository.getCurrentProfile().firstOrNull()
            }.getOrNull()
            val initialMediaVariant = profile?.gender?.toExerciseMediaVariant()

            val exercise = runCatching {
                catalogRepository.getExerciseById(exerciseId)
            }.getOrNull()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    exercise = exercise,
                    mediaVariant = initialMediaVariant,
                    isVideoPlaying = exercise.hasPlayableVideo(initialMediaVariant),
                    errorMessage = if (exercise == null) "Exercise details unavailable." else null,
                )
            }

            // Observe subsequent profile updates if user changes gender in settings
            profileRepository.getCurrentProfile().collect { updatedProfile ->
                val updatedVariant = updatedProfile.gender.toExerciseMediaVariant()
                _uiState.update { state ->
                    if (state.mediaVariant != updatedVariant) {
                        state.copy(
                            mediaVariant = updatedVariant,
                            isVideoPlaying = state.exercise.hasPlayableVideo(updatedVariant)
                        )
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun onAction(action: ExerciseInfoAction) {
        when (action) {
            ExerciseInfoAction.BackClicked -> Unit
            is ExerciseInfoAction.TabSelected -> {
                _uiState.update { it.copy(selectedTab = action.tab) }
            }
            ExerciseInfoAction.VideoPlaybackToggled -> {
                if (_uiState.value.exercise.hasPlayableVideo(_uiState.value.mediaVariant)) {
                    _uiState.update { it.copy(isVideoPlaying = !it.isVideoPlaying) }
                }
            }
        }
    }
}

private fun ExerciseDefinition?.hasPlayableVideo(mediaVariant: ExerciseMediaVariant?): Boolean {
    val exercise = this ?: return false
    return resolveExerciseInfoMediaAsset(
        exercise = exercise,
        mediaVariant = mediaVariant,
    )?.videoRef.isNullOrBlank().not()
}

private const val EXERCISE_ID_KEY = "exerciseId"
