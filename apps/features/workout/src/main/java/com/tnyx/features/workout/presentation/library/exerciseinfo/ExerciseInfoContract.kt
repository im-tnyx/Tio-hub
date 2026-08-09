package com.tnyx.features.workout.presentation.library.exerciseinfo

import androidx.compose.runtime.Immutable
import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant

enum class ExerciseInfoTab {
    SUMMARY,
    HISTORY,
    HOW_TO,
}

@Immutable
data class ExerciseInfoUiState(
    val isLoading: Boolean = true,
    val exercise: ExerciseDefinition? = null,
    val selectedTab: ExerciseInfoTab = ExerciseInfoTab.SUMMARY,
    val isVideoPlaying: Boolean = true,
    val mediaVariant: ExerciseMediaVariant? = null,
    val errorMessage: String? = null,
)

sealed interface ExerciseInfoAction {
    data object BackClicked : ExerciseInfoAction
    data class TabSelected(val tab: ExerciseInfoTab) : ExerciseInfoAction
    data object VideoPlaybackToggled : ExerciseInfoAction
}
