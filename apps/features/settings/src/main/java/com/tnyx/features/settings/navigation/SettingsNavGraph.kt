package com.tnyx.features.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.tnyx.features.settings.presentation.app_preferences.AppPreferencesRoute
import com.tnyx.features.settings.presentation.bottom_navigation.BottomNavigationRoute
import com.tnyx.features.settings.presentation.home.SettingsHomeRoute
import com.tnyx.routing.routes.SettingsRoute

fun NavGraphBuilder.settingsGraph(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    onOpenNutritionTargets: () -> Unit,
    onOpenAppPreferences: () -> Unit
) {
    navigation<SettingsRoute.Graph>(
        startDestination = SettingsRoute.Home
    ) {
        composable<SettingsRoute.Home> {
            SettingsHomeRoute(
                onNavigateBack = onNavigateBack,
                onOpenNutritionTargets = onOpenNutritionTargets,
                onOpenAppPreferences = onOpenAppPreferences,
                onOpenPersonalInfo = { navController.navigate(SettingsRoute.PersonalInfo) }
            )
        }

        composable<SettingsRoute.AppPreferences> {
            AppPreferencesRoute(
                onNavigateBack = onNavigateBack,
                onOpenBottomNavigation = {
                    navController.navigate(SettingsRoute.BottomNavigation)
                },
            )
        }

        composable<SettingsRoute.BottomNavigation> {
            BottomNavigationRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<SettingsRoute.Notifications> {
            SettingsHomeRoute(
                onNavigateBack = onNavigateBack,
                onOpenNutritionTargets = onOpenNutritionTargets,
                onOpenAppPreferences = onOpenAppPreferences,
                onOpenPersonalInfo = { navController.navigate(SettingsRoute.PersonalInfo) }
            )
        }

        composable<SettingsRoute.Units> {
            SettingsHomeRoute(
                onNavigateBack = onNavigateBack,
                onOpenNutritionTargets = onOpenNutritionTargets,
                onOpenAppPreferences = onOpenAppPreferences,
                onOpenPersonalInfo = { navController.navigate(SettingsRoute.PersonalInfo) }
            )
        }

        composable<SettingsRoute.Account> {
            SettingsHomeRoute(
                onNavigateBack = onNavigateBack,
                onOpenNutritionTargets = onOpenNutritionTargets,
                onOpenAppPreferences = onOpenAppPreferences,
                onOpenPersonalInfo = { navController.navigate(SettingsRoute.PersonalInfo) }
            )
        }

        composable<SettingsRoute.ExportData> {
            SettingsHomeRoute(
                onNavigateBack = onNavigateBack,
                onOpenNutritionTargets = onOpenNutritionTargets,
                onOpenAppPreferences = onOpenAppPreferences,
                onOpenPersonalInfo = { navController.navigate(SettingsRoute.PersonalInfo) }
            )
        }

        composable<SettingsRoute.About> {
            SettingsHomeRoute(
                onNavigateBack = onNavigateBack,
                onOpenNutritionTargets = onOpenNutritionTargets,
                onOpenAppPreferences = onOpenAppPreferences,
                onOpenPersonalInfo = { navController.navigate(SettingsRoute.PersonalInfo) }
            )
        }

        composable<SettingsRoute.PersonalInfo> {
            com.tnyx.features.settings.presentation.personal_info.PersonalInfoRoute(
                onNavigateBack = onNavigateBack
            )
        }
    }
}
