package com.tnyx.core.ui.shell.presentation.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.shell.presentation.action.ShellAction
import com.tnyx.core.ui.shell.presentation.state.ShellTab
import com.tnyx.core.ui.shell.presentation.state.ShellUiState
import com.tnyx.core.ui.shell.presentation.widgets.MainBottomNav
import com.tnyx.core.ui.shell.presentation.widgets.MainTopBar
import com.tnyx.core.ui.shell.presentation.widgets.WorkoutSecondaryNav
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main Application Shell Container.
 * Provides the visual frame (Glass Bottom Bar, Dynamic Top Bar, System UI handling).
 *
 * Workout tab secondary bar behavior:
 * - Enters with slide-up animation when Workout tab is selected.
 * - Hides (slides down) when user scrolls UP more than 200dp.
 * - Returns after scroll stops (1.5s delay) or when user scrolls DOWN.
 */
@Composable
fun TnyxShell(
    state: ShellUiState,
    onAction: (ShellAction) -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    // Height of the secondary nav bar (used for translation offset when hiding)
    val secondaryNavHeightPx = with(density) { 44.dp.toPx() }
    // Upward scroll threshold before secondary nav hides
    val scrollThresholdPx = with(density) { 200.dp.toPx() }

    // derivedStateOf: NestedScrollConnection reads this as a State snapshot —
    // no recomposition triggered unless the tab actually changes.
    //val isWorkoutTab by remember { derivedStateOf { state.selectedTab == ShellTab.Workout } }

    // Use state directly for visibility to ensure reactive updates
    val isWorkoutTab = state.selectedTab == ShellTab.Workout

    // State controlling secondary nav scroll-based visibility
    val secondaryNavVisibleState = remember { mutableStateOf(true) }
    val cumulativeUpScrollState = remember { mutableFloatStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()
    // var delegate (not mutableStateOf<Job?>) — Job is non-stable and doesn't need
    // to drive recomposition; it's only read inside the scroll callback.
    var restoreJob: Job? by remember { mutableStateOf(null) }

    // Reset scroll tracking whenever workout tab is entered or exited
    LaunchedEffect(isWorkoutTab) {
        secondaryNavVisibleState.value = true
        cumulativeUpScrollState.value = 0f
        restoreJob?.cancel()
        restoreJob = null
    }

    // Animated Y-offset for scroll-based hide/show (0 = visible, +height = hidden)
    val scrollHideOffset by animateFloatAsState(
        targetValue = if (secondaryNavVisibleState.value) 0f else secondaryNavHeightPx,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "workoutSecondaryNavScrollOffset"
    )

    // NestedScrollConnection intercepts scroll events from any scrollable inside content().
    // Keyed on nothing (plain remember) — coroutineScope is stable within a composition
    // and is safely captured in the closure. Keying on coroutineScope would recreate this
    // object unnecessarily and could cause scroll callbacks to be skipped mid-gesture.
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Only active on Workout tab — isWorkoutTab is a derivedStateOf snapshot read,
                // safe to call from a non-Compose thread (scroll thread).
                if (!isWorkoutTab) return Offset.Zero

                val delta = available.y
                when {
                    delta < 0f -> {
                        // ---- User scrolling UP ----
                        cumulativeUpScrollState.value += (-delta)

                        // Hide secondary nav after threshold is crossed
                        if (cumulativeUpScrollState.value > scrollThresholdPx) {
                            secondaryNavVisibleState.value = false
                        }

                        // Schedule auto-restore 1.5s after scroll stops
                        restoreJob?.cancel()
                        restoreJob = coroutineScope.launch {
                            delay(1_500L)
                            secondaryNavVisibleState.value = true
                            cumulativeUpScrollState.value = 0f
                        }
                    }
                    delta > 0f -> {
                        // ---- User scrolling DOWN → immediately restore ----
                        cumulativeUpScrollState.value = 0f
                        secondaryNavVisibleState.value = true
                        restoreJob?.cancel()
                        restoreJob = null
                    }
                }
                return Offset.Zero // We never consume the scroll, just observe it
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background)
            .nestedScroll(nestedScrollConnection)
    ) {
        // ── Screen content (scrollable screens live here) ──
        content()

        // ── Top navigation bar (Home tab only) ──
        val showTopBar = state.selectedTab == ShellTab.Home
        if (showTopBar) {
            MainTopBar(
                planTier = state.planTier,
                scrollOpacity = state.appBarOpacity,
                onAction = onAction,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // ── Bottom chrome: secondary nav stacked above the main bottom nav ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            // Workout secondary tab bar:
            //  - AnimatedVisibility controls entry (slide up) / exit (slide down) on tab switch.
            //  - graphicsLayer translationY controls scroll-based hide/show within the tab.
            AnimatedVisibility(
                visible = isWorkoutTab,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                )
            ) {
                WorkoutSecondaryNav(
                    selectedTab = state.selectedWorkoutTab,
                    onTabSelected = { onAction(ShellAction.WorkoutSubTabSelected(it)) },
                    modifier = Modifier.graphicsLayer {
                        translationY = scrollHideOffset
                    }
                )
            }

            // Main bottom navigation bar
            if (state.isBottomNavVisible) {
                MainBottomNav(
                    selectedTab = state.selectedTab,
                    onTabSelected = { onAction(ShellAction.TabSelected(it)) }
                )
            }
        }
    }
}
