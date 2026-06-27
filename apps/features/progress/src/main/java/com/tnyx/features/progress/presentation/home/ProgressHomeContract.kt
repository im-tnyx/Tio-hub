package com.tnyx.features.progress.presentation.home

data class ProgressHomeUiState(
    val title: String = "Progress",
    val subtitle: String = "Journey, photos, measurements, weight, achievements"
)

sealed interface ProgressHomeAction {
    data object JourneyClicked : ProgressHomeAction
    data object PhotosClicked : ProgressHomeAction
}
