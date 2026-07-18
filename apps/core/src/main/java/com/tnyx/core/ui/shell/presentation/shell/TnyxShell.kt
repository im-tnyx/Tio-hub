package com.tnyx.core.ui.shell.presentation.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.shell.domain.model.ShellTab
import com.tnyx.core.ui.shell.presentation.action.ShellAction
import com.tnyx.core.ui.shell.presentation.state.ShellUiState
import com.tnyx.core.ui.shell.presentation.widgets.MainBottomNav
import com.tnyx.core.ui.shell.presentation.widgets.MainTopBar

/**
 * Main application shell container.
 *
 * The shell owns app-level chrome only: top bar, bottom navigation, and screen
 * content placement. Feature-specific navigation stays inside feature graphs.
 */
@Composable
fun TnyxShell(
    state: ShellUiState,
    onAction: (ShellAction) -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background),
    ) {
        content()

        if (state.selectedTab == ShellTab.Home) {
            MainTopBar(
                avatar = state.avatar,
                scrollOpacity = state.appBarOpacity,
                onAction = onAction,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }

        if (state.isBottomNavVisible) {
            MainBottomNav(
                tabs = state.bottomTabs,
                selectedTab = state.selectedTab,
                onTabSelected = { onAction(ShellAction.TabSelected(it)) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
