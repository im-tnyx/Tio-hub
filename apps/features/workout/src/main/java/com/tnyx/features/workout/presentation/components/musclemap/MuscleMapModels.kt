package com.tnyx.features.workout.presentation.components.musclemap

import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant

/** Which side of the body to render in a [TioMuscleMap]. */
enum class MuscleMapView {
    FRONT,
    BACK,
}

/**
 * Broad muscle-region categories used for grouping and fallback asset resolution.
 * Each entry declares human-readable aliases that normalize to the same region.
 */
enum class MuscleMapRegionKey(vararg aliases: String) {
    ABS("abs", "abdominals", "core", "rectus abdominis"),
    BACK("back", "lats", "latissimus dorsi", "erector spinae"),
    BICEPS("bicep", "biceps"),
    CALVES("calf", "calves", "gastrocnemius", "soleus"),
    CHEST("chest", "pectorals", "pecs"),
    FOREARMS("forearm", "forearms", "wrist extensors", "wrist flexors"),
    HAMSTRINGS("hamstring", "hamstrings"),
    HIPS("hip", "hips", "hip adductors", "glutes", "gluteus"),
    NECK("neck"),
    QUADRICEPS("quadriceps", "quad", "quads"),
    SHOULDERS("shoulder", "shoulders", "deltoid", "deltoids"),
    TRICEPS("tricep", "triceps"),
    ;

    internal val normalizedAliases: Set<String> = aliases
        .mapTo(mutableSetOf(), String::normalizeMuscleMapKey)
        .plus(name.normalizeMuscleMapKey())
}

/**
 * Resolved set of layered image assets for rendering a [TioMuscleMap].
 * The first entry in each view's list is always the grey base body.
 */
data class MuscleMapLayerSet(
    val variant: ExerciseMediaVariant,
    val frontBaseAsset: String,
    val backBaseAsset: String,
    val frontOverlayAssets: List<String>,
    val backOverlayAssets: List<String>,
) {
    fun assetsFor(view: MuscleMapView): List<String> = when (view) {
        MuscleMapView.FRONT -> listOf(frontBaseAsset) + frontOverlayAssets
        MuscleMapView.BACK  -> listOf(backBaseAsset)  + backOverlayAssets
    }
}

internal val muscleMapSeparator = Regex("[^a-z0-9]+")

internal fun String.normalizeMuscleMapKey(): String = trim()
    .lowercase(java.util.Locale.ROOT)
    .replace(muscleMapSeparator, "_")
    .trim('_')
