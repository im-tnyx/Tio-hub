package com.tnyx.data.profile

import com.tnyx.shared.auth.domain.model.AuthSession
import com.tnyx.shared.profile.domain.model.UserProfile

internal const val GUEST_PROFILE_ID = "local-guest"

internal fun AuthSession?.localProfileId(): String = this?.userId ?: GUEST_PROFILE_ID

internal fun emptyLocalProfile(session: AuthSession?): UserProfile {
    return UserProfile(
        id = session.localProfileId(),
        displayName = session?.displayName.orEmpty(),
        dob = "",
        gender = "",
        planLabel = "",
        weight = 0.0,
        height = 0,
        bmi = 0.0,
        bmr = 0,
        username = session?.email?.substringBefore("@").orEmpty(),
    )
}
