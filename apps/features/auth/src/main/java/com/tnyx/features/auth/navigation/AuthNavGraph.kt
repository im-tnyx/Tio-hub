package com.tnyx.features.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.tnyx.features.auth.presentation.login.LoginRoute
import com.tnyx.features.auth.presentation.otp.OtpVerificationRoute
import com.tnyx.features.auth.presentation.signup.SignupRoute
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
            LoginRoute(
                onAuthSuccess = onAuthSuccess,
                onNavigateToSignup = {
                    navController.navigate(AuthRoute.Signup)
                }
            )
        }

        composable<AuthRoute.Signup> {
            SignupRoute(
                onNavigateToLogin = {
                    if (!navController.popBackStack()) {
                        navController.navigate(AuthRoute.Login)
                    }
                },
                onNavigateToOtp = { email ->
                    navController.navigate(AuthRoute.OtpVerification(email))
                },
                onAuthSuccess = onAuthSuccess
            )
        }

        composable<AuthRoute.OtpVerification> { backStackEntry ->
            val route = backStackEntry.toRoute<AuthRoute.OtpVerification>()
            OtpVerificationRoute(
                email = route.email,
                onAuthSuccess = onAuthSuccess,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
