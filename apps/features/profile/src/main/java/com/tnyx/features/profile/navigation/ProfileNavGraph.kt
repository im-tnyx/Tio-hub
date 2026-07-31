package com.tnyx.features.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.tnyx.features.profile.presentation.avatar_viewer.AvatarViewerRoute
import com.tnyx.features.profile.presentation.home.ProfileHomeRoute
import com.tnyx.routing.routes.ProfileRoute

fun NavGraphBuilder.profileGraph(
    navController: NavHostController,
    onOpenSettings: () -> Unit,
    onOpenPersonalInfo: () -> Unit,
    onNavigateBack: () -> Unit
) {
    navigation<ProfileRoute.Graph>(
        startDestination = ProfileRoute.Home
    ) {
        composable<ProfileRoute.Home> {
            ProfileHomeRoute(
                onOpenSettings = onOpenSettings,
                onOpenEditProfile = onOpenPersonalInfo,
                onOpenAvatarViewer = {
                    navController.navigate(ProfileRoute.AvatarViewer)
                },
                onNavigateBack = onNavigateBack,
                showBackButton = true,
            )
        }

        composable<ProfileRoute.AvatarViewer> {
            AvatarViewerRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
