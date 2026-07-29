package com.tnyx.features.profile.presentation.home

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileHomeViewModelTest {

    @Test
    fun persistedUsernameWinsAndRemovesDisplayPrefix() {
        assertEquals(
            "santosh_jangid",
            resolveProfileUsername(
                username = "@Santosh_Jangid",
                displayName = "Different Name",
            ),
        )
    }

    @Test
    fun blankUsernameFallsBackToNormalizedDisplayName() {
        assertEquals(
            "santosh_jangid",
            resolveProfileUsername(
                username = "",
                displayName = "Santosh Jangid",
            ),
        )
    }

    @Test
    fun missingIdentityUsesStablePlaceholder() {
        assertEquals(
            "username",
            resolveProfileUsername(
                username = "",
                displayName = "",
            ),
        )
    }
}
