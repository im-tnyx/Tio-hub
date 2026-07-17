package com.tnyx.routing

import com.tnyx.core.ui.shell.domain.model.ShellTab
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileEntryPolicyTest {

    @Test
    fun `avatar opens standalone profile when You is disabled`() {
        assertEquals(
            ProfileEntryAction.OpenStandaloneProfile,
            resolveProfileEntryAction(
                bottomTabs = listOf(
                    ShellTab.Home,
                    ShellTab.Nutrition,
                    ShellTab.Ai,
                ),
                selectedTab = ShellTab.Nutrition,
            ),
        )
    }

    @Test
    fun `avatar selects You when enabled from another tab`() {
        assertEquals(
            ProfileEntryAction.SelectYouTab,
            resolveProfileEntryAction(
                bottomTabs = listOf(
                    ShellTab.Home,
                    ShellTab.Workout,
                    ShellTab.You,
                ),
                selectedTab = ShellTab.Workout,
            ),
        )
    }

    @Test
    fun `avatar is a no-op when You is already active`() {
        assertEquals(
            ProfileEntryAction.NoOp,
            resolveProfileEntryAction(
                bottomTabs = listOf(
                    ShellTab.Home,
                    ShellTab.Nutrition,
                    ShellTab.You,
                ),
                selectedTab = ShellTab.You,
            ),
        )
    }
}
