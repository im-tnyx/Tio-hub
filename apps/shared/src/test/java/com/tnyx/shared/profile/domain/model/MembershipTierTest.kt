package com.tnyx.shared.profile.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class MembershipTierTest {

    @Test
    fun `blank and unknown labels map to free`() {
        assertEquals(MembershipTier.Free, MembershipTier.fromPlanLabel(null))
        assertEquals(MembershipTier.Free, MembershipTier.fromPlanLabel(""))
        assertEquals(MembershipTier.Free, MembershipTier.fromPlanLabel("starter"))
    }

    @Test
    fun `plus labels map to plus`() {
        assertEquals(MembershipTier.Plus, MembershipTier.fromPlanLabel("plus"))
        assertEquals(MembershipTier.Plus, MembershipTier.fromPlanLabel("TNYX PLUS"))
    }

    @Test
    fun `premium and pro labels map to premium`() {
        assertEquals(MembershipTier.Premium, MembershipTier.fromPlanLabel("premium"))
        assertEquals(MembershipTier.Premium, MembershipTier.fromPlanLabel("Tio Pro"))
    }
}
