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
import com.tnyx.core.ui.shell.presentation.state.WorkoutSubTab
import com.tnyx.features.nutrition.navigation.NutritionScreen
import com.tnyx.features.workout.navigation.WorkoutScreen
import com.tnyx.routing.graphs.mainGraph
import com.tnyx.routing.routes.MainRoute
import com.tnyx.routing.routes.ProfileRoute

/**
 * Main Shell with Bottom Navigation Bar.
 * Implementation based on NAVIGATION_GUIDE.md for scalability.
 *
 * Workout secondary nav (History / Explore / Routines) is driven by
 * the main NavController navigating within WorkoutNavGraph.
 * Shell only shows/hides the bar; all workout sub-screen logic lives
 * inside the Workout feature (WorkoutNavGraph).
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

    // Map current destination to ShellTab
    val selectedTab = when {
        currentDestination?.hierarchy?.any { it.hasRoute(MainRoute.WorkoutGraph::class) } == true ||
        currentDestination?.hasRoute(WorkoutScreen.History::class) == true ||
        currentDestination?.hasRoute(WorkoutScreen.Explore::class) == true ||
        currentDestination?.hasRoute(WorkoutScreen.Routines::class) == true -> ShellTab.Workout

        currentDestination?.hierarchy?.any { it.hasRoute(MainRoute.Home::class) } == true -> ShellTab.Home
        currentDestination?.hierarchy?.any { it.hasRoute(MainRoute.NutritionGraph::class) } == true -> ShellTab.Nutrition
        currentDestination?.hierarchy?.any { it.hasRoute(MainRoute.AiCoach::class) } == true -> ShellTab.Ai
        currentDestination?.hierarchy?.any { it.hasRoute(MainRoute.ProgressGraph::class) } == true -> ShellTab.Progress
        else -> ShellTab.Home
    }

    // Derive active workout sub-tab from current nav back-stack destination.
    // No separate state needed — truth comes from NavController.
    val selectedWorkoutTab = when {
        currentDestination?.hierarchy?.any { it.hasRoute<WorkoutScreen.Explore>() } == true -> WorkoutSubTab.Explore
        currentDestination?.hierarchy?.any { it.hasRoute<WorkoutScreen.Routines>() } == true -> WorkoutSubTab.Routines
        else -> WorkoutSubTab.History
    }

    val isBottomNavVisible = when {
        currentDestination?.hasRoute<NutritionScreen.MealEditor>() == true -> false
        currentDestination?.hasRoute<NutritionScreen.MealItemEditor>() == true -> false
        else -> true
    }

    TnyxShell(
        state = ShellUiState(
            selectedTab = selectedTab,
            isBottomNavVisible = isBottomNavVisible,
            selectedWorkoutTab = selectedWorkoutTab
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
                is ShellAction.WorkoutSubTabSelected -> {
                    // Secondary nav tabs drive WorkoutNavGraph navigation directly.
                    val workoutRoute = when (action.tab) {
                        WorkoutSubTab.History -> WorkoutScreen.History
                        WorkoutSubTab.Explore -> WorkoutScreen.Explore
                        WorkoutSubTab.Routines -> WorkoutScreen.Routines
                    }
                    mainNavController.navigate(workoutRoute) {
                        popUpTo<MainRoute.WorkoutGraph> { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                is ShellAction.ProfileClicked -> {
                    rootNavController.navigate(ProfileRoute.Graph)
                }
                else -> { /* Other shell actions handled upstream */ }
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
