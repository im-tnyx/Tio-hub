package com.tnyx.routing

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.tnyx.core.legal.presentation.route.LegalRoute
import com.tnyx.features.splash.presentation.route.SplashRoute
import com.tnyx.features.auth.navigation.authGraph
import com.tnyx.features.profile.navigation.profileGraph
import com.tnyx.features.settings.navigation.settingsGraph
import com.tnyx.features.welcome.navigation.welcomeScreen
import com.tnyx.routing.routes.RootRoute
import com.tnyx.routing.routes.SettingsRoute

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = RootRoute.Splash,
        modifier = modifier
    ) {
        composable<RootRoute.Splash> {
            SplashRoute(
                onNavigateToWelcome = {
                    navController.navigate(RootRoute.Welcome) {
                        popUpTo(RootRoute.Splash) { inclusive = true }
                    }
                }
            )
        }

        welcomeScreen(
            onNavigateToHome = {
                navController.navigate(RootRoute.MainGraph) {
                    popUpTo(RootRoute.Welcome) { inclusive = true }
                }
            },
            onNavigateToLogin = {
                navController.navigate(RootRoute.AuthGraph)
            },
            onNavigateToLegal = { title, url ->
                navController.navigate(RootRoute.Legal(title = title, url = url))
            }
        )

        composable<RootRoute.MainGraph> {
            MainScreen(rootNavController = navController)
        }

        authGraph(
            navController = navController,
            onAuthSuccess = {
                navController.navigate(RootRoute.MainGraph) {
                    popUpTo(RootRoute.AuthGraph) { inclusive = true }
                }
            }
        )

        profileGraph(
            navController = navController,
            onOpenSettings = {
                navController.navigate(SettingsRoute.Graph)
            },
            onOpenProgress = {
                navController.navigate(RootRoute.MainGraph) {
                    popUpTo(RootRoute.MainGraph) { inclusive = false }
                }
            },
            onNavigateBack = {
                navController.popBackStack()
            }
        )

        settingsGraph(
            navController = navController,
            onNavigateBack = {
                navController.popBackStack()
            }
        )

        dialog<RootRoute.Legal> { backStackEntry ->
            val args = backStackEntry.toRoute<RootRoute.Legal>()
            LegalRoute(
                title = args.title,
                url = args.url,
                isRemoteEnabled = args.isRemoteEnabled,
                onClose = { navController.popBackStack() }
            )
        }
    }
}
