package com.tnyx.routing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.tnyx.core.legal.presentation.route.LegalRoute
import com.tnyx.features.onboarding.domain.model.OnboardingAuthState
import com.tnyx.features.onboarding.domain.model.OnboardingEntryPath
import com.tnyx.features.splash.presentation.route.SplashRoute
import com.tnyx.features.auth.navigation.authGraph
import com.tnyx.features.nutrition.presentation.targets.NutritionTargetsRoute
import com.tnyx.features.onboarding.navigation.onboardingScreen
import com.tnyx.features.profile.navigation.profileGraph
import com.tnyx.features.settings.navigation.settingsGraph
import com.tnyx.features.welcome.navigation.welcomeScreen
import com.tnyx.routing.routes.NutritionRoute
import com.tnyx.routing.routes.ProfileRoute
import com.tnyx.routing.routes.RootRoute
import com.tnyx.routing.routes.SettingsRoute

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sessionViewModel: AppSessionViewModel = hiltViewModel(),
) {
    LaunchedEffect(sessionViewModel) {
        sessionViewModel.effect.collect { effect ->
            when (effect) {
                AppSessionEffect.SignedOut -> {
                    navController.navigate(RootRoute.AuthGraph) {
                        popUpTo(RootRoute.MainGraph) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

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
                },
                onNavigateToOnboarding = {
                    navController.navigate(
                        RootRoute.Onboarding(
                            entryPath = OnboardingEntryPath.SignIn.name,
                            authState = OnboardingAuthState.SignedIn.name,
                            signupCompleted = true,
                        ),
                    ) {
                        popUpTo(RootRoute.Splash) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(RootRoute.MainGraph) {
                        popUpTo(RootRoute.Splash) { inclusive = true }
                    }
                },
            )
        }

        welcomeScreen(
            onNavigateToOnboarding = {
                navController.navigate(
                    RootRoute.Onboarding(
                        entryPath = OnboardingEntryPath.GetStarted.name,
                    ),
                ) {
                    launchSingleTop = true
                }
            },
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

        onboardingScreen(
            onCompleted = {
                navController.navigate(RootRoute.MainGraph) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onExit = {
                navController.popBackStack()
            },
        )

        composable<RootRoute.MainGraph> {
            MainScreen(rootNavController = navController)
        }

        authGraph(
            navController = navController,
            onAuthSuccess = {
                navController.navigate(
                    RootRoute.Onboarding(
                        entryPath = OnboardingEntryPath.SignIn.name,
                        authState = OnboardingAuthState.SignedIn.name,
                        signupCompleted = true,
                    ),
                ) {
                    popUpTo(RootRoute.AuthGraph) { inclusive = true }
                }
            }
        )

        profileGraph(
            navController = navController,
            onOpenSettings = {
                navController.navigate(SettingsRoute.Graph)
            },
            onOpenPersonalInfo = {
                navController.navigate(SettingsRoute.PersonalInfo)
            },
            onNavigateBack = {
                navController.popBackStack()
            }
        )

        settingsGraph(
            navController = navController,
            onNavigateBack = {
                navController.popBackStack()
            },
            onOpenNutritionTargets = {
                navController.navigate(NutritionRoute.Targets)
            },
            onOpenAppPreferences = {
                navController.navigate(SettingsRoute.AppPreferences)
            },
            onLogout = sessionViewModel::signOut,
        )

        composable<SettingsRoute.PersonalInfo> {
            com.tnyx.features.settings.presentation.personal_info.PersonalInfoRoute(
                onNavigateBack = { navController.popBackStack() },
                onOpenAvatarViewer = { navController.navigate(ProfileRoute.AvatarViewer) },
            )
        }

        composable<NutritionRoute.Targets> {
            NutritionTargetsRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }

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
