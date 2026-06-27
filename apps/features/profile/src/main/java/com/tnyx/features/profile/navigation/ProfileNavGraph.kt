package com.tnyx.features.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.tnyx.features.profile.presentation.home.ProfileHomeRoute
import com.tnyx.routing.routes.ProfileRoute

fun NavGraphBuilder.profileGraph(
    navController: NavHostController,
    onOpenSettings: () -> Unit,
    onOpenProgress: () -> Unit
) {
    navigation<ProfileRoute.Graph>(
        startDestination = ProfileRoute.Home
    ) {
        composable<ProfileRoute.Home> {
            ProfileHomeRoute(
                onOpenSettings = onOpenSettings,
                onOpenProgress = onOpenProgress
            )
        }

        composable<ProfileRoute.PersonalInfo> {
            ProfileHomeRoute(
                onOpenSettings = onOpenSettings,
                onOpenProgress = onOpenProgress
            )
        }
    }
}
