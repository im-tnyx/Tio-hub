package com.tnyx.wear.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface WearRoute {
    @Serializable
    data object Home : WearRoute

    @Serializable
    data object Workout : WearRoute

    @Serializable
    data object Nutrition : WearRoute
}
