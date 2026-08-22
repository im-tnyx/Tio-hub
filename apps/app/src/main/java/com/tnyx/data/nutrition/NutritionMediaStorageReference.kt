package com.tnyx.data.nutrition

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

internal const val NUTRITION_MEDIA_BUCKET = "tio-nutrition-media"

private const val NUTRITION_MEDIA_REFERENCE_PREFIX =
    "supabase-storage://$NUTRITION_MEDIA_BUCKET/"

private val nutritionMediaUrlMarkers = listOf(
    "/storage/v1/object/public/$NUTRITION_MEDIA_BUCKET/",
    "/storage/v1/object/sign/$NUTRITION_MEDIA_BUCKET/",
)

internal fun String.toNutritionMediaObjectPath(): String? {
    val encodedPath = when {
        startsWith(NUTRITION_MEDIA_REFERENCE_PREFIX) -> removePrefix(NUTRITION_MEDIA_REFERENCE_PREFIX)
        else -> nutritionMediaUrlMarkers.firstNotNullOfOrNull { marker ->
            substringAfter(marker, missingDelimiterValue = "").takeIf(String::isNotBlank)
        }
    }?.substringBefore('?') ?: return null

    val decodedPath = runCatching {
        URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.toString())
    }.getOrNull() ?: return null

    return decodedPath.takeIf(String::isSafeNutritionMediaObjectPath)
}

internal fun String.toOwnedNutritionMediaObjectPath(ownerUserId: String): String? {
    return toNutritionMediaObjectPath()
        ?.takeIf { path -> path.substringBefore('/') == ownerUserId }
}

internal fun String.toNutritionMediaStorageReference(): String {
    return "$NUTRITION_MEDIA_REFERENCE_PREFIX$this"
}

private fun String.isSafeNutritionMediaObjectPath(): Boolean {
    if (isBlank() || startsWith('/') || contains('\\')) return false
    return split('/').all { segment ->
        segment.isNotBlank() && segment != "." && segment != ".."
    }
}
