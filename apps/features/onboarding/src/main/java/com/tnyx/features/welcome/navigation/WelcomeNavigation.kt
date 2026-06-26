package com.tnyx.features.welcome.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.tnyx.features.welcome.presentation.route.WelcomeRoute
import com.tnyx.routing.routes.RootRoute

fun NavController.navigateToWelcome(navOptions: NavOptions? = null) {
    this.navigate(RootRoute.Welcome, navOptions)
}

fun NavGraphBuilder.welcomeScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToLegal: (title: String, url: String) -> Unit
) {
    composable<RootRoute.Welcome> {
        WelcomeRoute(
            onNavigateToHome = onNavigateToHome,
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToLegal = onNavigateToLegal
        )
    }
}
