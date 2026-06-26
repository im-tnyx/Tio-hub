package com.tnyx.features.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.tnyx.routing.routes.AuthRoute
import com.tnyx.routing.routes.RootRoute

/**
 * Nested Graph for Authentication flow.
 * Feature-owned navigation.
 */
fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    onAuthSuccess: () -> Unit
) {
    navigation<RootRoute.AuthGraph>(
        startDestination = AuthRoute.Login
    ) {
        composable<AuthRoute.Login> {
            // TODO: LoginRoute(...)
        }
        
        composable<AuthRoute.Signup> {
            // TODO: SignupRoute(...)
        }
        
        composable<AuthRoute.OtpVerification> {
            // TODO: OtpRoute(...)
        }
    }
}
