package com.tnyx.wear.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface WearRoute {
    @Serializable
    data object Home : WearRoute

    @Serializable
    data object Workout : WearRoute

    @Serializable
    data object Nutrition : WearRoute

    @Serializable
    data object AddWater : WearRoute

    @Serializable
    data object Summary : WearRoute

    @Serializable
    data object CalorieSummary : WearRoute

    @Serializable
    data object UnitsSettings : WearRoute

    @Serializable
    data object CalorieInput : WearRoute

    @Serializable
    data object LogFood : WearRoute
}
