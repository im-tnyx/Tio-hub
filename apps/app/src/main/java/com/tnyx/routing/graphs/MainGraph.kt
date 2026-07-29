package com.tnyx.routing.graphs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.shell.domain.model.ShellTab
import com.tnyx.core.ui.shell.domain.model.deriveHomeExperienceMode
import com.tnyx.features.home.presentation.home.HomeRoute
import com.tnyx.features.nutrition.navigation.nutritionGraph
import com.tnyx.features.profile.presentation.home.ProfileHomeRoute
import com.tnyx.features.progress.navigation.progressGraph
import com.tnyx.features.workout.navigation.workoutGraph
import com.tnyx.routing.routes.MainRoute

/**
 * Nested Graph for Main Shell (Bottom Navigation).
 */
fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    enabledTabs: List<ShellTab>,
    onOpenSettings: () -> Unit,
) {
    composable<MainRoute.Home> {
        HomeRoute(
            mode = deriveHomeExperienceMode(enabledTabs),
        )
    }

    nutritionGraph(
        navController = navController,
        onShowOverview = { /* Handle */ },
    )

    composable<MainRoute.MealPlan> {
        TopLevelFoundationScreen(
            title = "Meal Plan",
            description = "Daily and weekly plans, meal suggestions and future grocery planning live here.",
        )
    }

    composable<MainRoute.AiCoach> {
        TopLevelFoundationScreen(
            title = "Tio",
            description = "Personal coaching and cross-domain suggestions live here.",
        )
    }

    workoutGraph(navController = navController)

    composable<MainRoute.WorkoutLibrary> {
        TopLevelFoundationScreen(
            title = "Library",
            description = "Exercises, saved routines, programs and templates live here.",
        )
    }

    progressGraph(navController = navController)

    composable<MainRoute.You> {
        YouProfileRoute(
            enabledTabs = enabledTabs,
            navController = navController,
            onOpenSettings = onOpenSettings,
        )
    }
}

@Composable
private fun YouProfileRoute(
    enabledTabs: List<ShellTab>,
    navController: NavHostController,
    onOpenSettings: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        ProfileHomeRoute(
            onOpenSettings = onOpenSettings,
            onOpenProgress = {
                if (ShellTab.Progress in enabledTabs) {
                    navController.navigate(MainRoute.ProgressGraph)
                } else {
                    navController.navigate(MainRoute.Home)
                }
            },
            onNavigateBack = {
                // You is a top-level tab. Its header intentionally has no Back action.
            },
        )

        YouHeader(
            onOpenSettings = onOpenSettings,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun YouHeader(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TnyxTheme.colors.surfaceRaised)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = TnyxTheme.dimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .clearAndSetSemantics {},
        )
        Text(
            text = "You",
            style = TnyxTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary,
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f),
        )
        IconButton(onClick = onOpenSettings) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = TnyxTheme.colors.textPrimary,
            )
        }
    }
}

@Composable
private fun TopLevelFoundationScreen(
    title: String,
    description: String,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
            )
            Text(text = description)
        }
    }
}
