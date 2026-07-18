package com.tnyx.core.ui.components.avatar

import org.junit.Assert.assertEquals
import org.junit.Test

class TnyxUserAvatarTest {

    @Test
    fun `blank display name has no initials`() {
        assertEquals("", avatarInitials("   "))
    }

    @Test
    fun `single name uses first letter`() {
        assertEquals("S", avatarInitials("santosh"))
    }

    @Test
    fun `multiple names use first two initials`() {
        assertEquals("SJ", avatarInitials("Santosh Jangid"))
        assertEquals("TS", avatarInitials("  tio   user  sample "))
    }
}
