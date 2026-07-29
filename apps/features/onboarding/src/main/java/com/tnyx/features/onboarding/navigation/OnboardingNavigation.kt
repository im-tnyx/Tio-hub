package com.tnyx.features.onboarding.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tnyx.features.onboarding.presentation.OnboardingRoute
import com.tnyx.routing.routes.RootRoute

fun NavGraphBuilder.onboardingScreen(
    onCompleted: () -> Unit,
    onExit: () -> Unit,
) {
    composable<RootRoute.Onboarding> {
        OnboardingRoute(
            onCompleted = onCompleted,
            onExit = onExit,
        )
    }
}
