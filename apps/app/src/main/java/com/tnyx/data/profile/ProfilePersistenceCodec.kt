package com.tnyx.data.profile

import com.tnyx.shared.profile.domain.model.UserProfile
import kotlinx.serialization.json.Json

class ProfilePersistenceCodec(
    private val json: Json = Json {
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
    },
) {
    fun encode(profile: UserProfile): String {
        return json.encodeToString(UserProfile.serializer(), profile)
    }

    fun decode(profileJson: String): UserProfile {
        return json.decodeFromString(UserProfile.serializer(), profileJson)
    }
}
