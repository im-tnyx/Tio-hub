package com.tnyx.routing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tnyx.core.ui.shell.presentation.action.ShellAction
import com.tnyx.core.ui.shell.presentation.shell.TnyxShell
import com.tnyx.core.ui.shell.presentation.state.ShellTab
import com.tnyx.core.ui.shell.presentation.state.ShellUiState
import com.tnyx.features.nutrition.navigation.NutritionScreen
import com.tnyx.routing.graphs.mainGraph
import com.tnyx.routing.routes.MainRoute
import com.tnyx.routing.routes.ProfileRoute
import com.tnyx.routing.routes.SettingsRoute

/**
 * Main shell with bottom navigation.
 */
@Composable
fun MainScreen(
    @Suppress("UNUSED_PARAMETER") rootNavController: NavHostController,
) {
    val mainNavController = rememberNavController()
    val navActions = remember(mainNavController) {
        TnyxNavigationActions(mainNavController)
    }

    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val selectedTab = when {
        currentDestination?.hierarchy?.any { it.hasRoute(MainRoute.WorkoutGraph::class) } == true -> ShellTab.Workout
        currentDestination?.hierarchy?.any { it.hasRoute(MainRoute.Home::class) } == true -> ShellTab.Home
        currentDestination?.hierarchy?.any { it.hasRoute(MainRoute.NutritionGraph::class) } == true -> ShellTab.Nutrition
        currentDestination?.hierarchy?.any { it.hasRoute(MainRoute.AiCoach::class) } == true -> ShellTab.Ai
        currentDestination?.hierarchy?.any { it.hasRoute(MainRoute.ProgressGraph::class) } == true -> ShellTab.Progress
        else -> ShellTab.Home
    }

    val isBottomNavVisible = when {
        currentDestination?.hasRoute<NutritionScreen.MealEditor>() == true -> false
        currentDestination?.hasRoute<NutritionScreen.MealItemEditor>() == true -> false
        else -> true
    }

    TnyxShell(
        state = ShellUiState(
            selectedTab = selectedTab,
            isBottomNavVisible = isBottomNavVisible
        ),
        onAction = { action ->
            when (action) {
                is ShellAction.TabSelected -> {
                    val route = when (action.tab) {
                        ShellTab.Home -> MainRoute.Home
                        ShellTab.Nutrition -> MainRoute.NutritionGraph
                        ShellTab.Ai -> MainRoute.AiCoach
                        ShellTab.Workout -> MainRoute.WorkoutGraph
                        ShellTab.Progress -> MainRoute.ProgressGraph
                    }
                    navActions.navigateToTopLevelDestination(route)
                }
                is ShellAction.ProfileClicked -> {
                    rootNavController.navigate(ProfileRoute.Graph)
                }
                is ShellAction.SettingsClicked -> {
                    rootNavController.navigate(SettingsRoute.Graph)
                }
                else -> {
                    // Other shell actions are handled by their owning surfaces.
                }
            }
        }
    ) {
        NavHost(
            navController = mainNavController,
            startDestination = MainRoute.Home
        ) {
            mainGraph(navController = mainNavController)
        }
    }
}
