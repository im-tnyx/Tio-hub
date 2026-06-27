package com.tnyx.features.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.tnyx.features.settings.presentation.home.SettingsHomeRoute
import com.tnyx.routing.routes.SettingsRoute

fun NavGraphBuilder.settingsGraph(
    navController: NavHostController,
    onNavigateBack: () -> Unit
) {
    navigation<SettingsRoute.Graph>(
        startDestination = SettingsRoute.Home
    ) {
        composable<SettingsRoute.Home> {
            SettingsHomeRoute(onNavigateBack = onNavigateBack)
        }

        composable<SettingsRoute.AppPreferences> {
            SettingsHomeRoute(onNavigateBack = onNavigateBack)
        }

        composable<SettingsRoute.Notifications> {
            SettingsHomeRoute(onNavigateBack = onNavigateBack)
        }

        composable<SettingsRoute.Units> {
            SettingsHomeRoute(onNavigateBack = onNavigateBack)
        }

        composable<SettingsRoute.Account> {
            SettingsHomeRoute(onNavigateBack = onNavigateBack)
        }

        composable<SettingsRoute.ExportData> {
            SettingsHomeRoute(onNavigateBack = onNavigateBack)
        }

        composable<SettingsRoute.About> {
            SettingsHomeRoute(onNavigateBack = onNavigateBack)
        }
    }
}
