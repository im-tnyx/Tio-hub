package com.tnyx.features.workout.presentation.components.musclemap

import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant
import java.util.Locale

enum class MuscleMapView {
    FRONT,
    BACK,
}

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

data class MuscleMapLayerSet(
    val variant: ExerciseMediaVariant,
    val frontBaseAsset: String,
    val backBaseAsset: String,
    val frontOverlayAssets: List<String>,
    val backOverlayAssets: List<String>,
) {
    fun assetsFor(view: MuscleMapView): List<String> = when (view) {
        MuscleMapView.FRONT -> listOf(frontBaseAsset) + frontOverlayAssets
        MuscleMapView.BACK -> listOf(backBaseAsset) + backOverlayAssets
    }
}

object MuscleMapAssetRegistry {
    private val regionsByAlias: Map<String, MuscleMapRegionKey> = buildMap {
        MuscleMapRegionKey.entries.forEach { region ->
            region.normalizedAliases.forEach { alias -> put(alias, region) }
        }
    }

    private val artworkByRegion = mapOf(
        MuscleMapRegionKey.ABS to artwork(
            front = gendered(
                male = listOf("front_rectus_abdominis_male.webp", "front_obliques_male.webp"),
                female = listOf("front_rectus_abdominis_female.webp", "front_obliques_female.webp"),
            ),
            back = gendered(
                male = listOf("back_obliques_male.webp"),
                female = listOf("back_obliques_female.webp"),
            ),
        ),
        MuscleMapRegionKey.BACK to artwork(
            back = gendered(
                male = listOf("back_latissimus_dorsi_male.webp", "back_erector_spinae_male.webp"),
                female = listOf("back_latissimus_dorsi_female.webp", "back_erector_spinae_female.webp"),
            ),
        ),
        MuscleMapRegionKey.BICEPS to artwork(
            front = gendered(
                male = listOf("front_biceps_male.webp"),
                female = listOf("front_biceps_female.webp"),
            ),
            back = gendered(
                male = listOf("back_biceps_male.webp"),
                female = listOf("back_biceps_female.webp"),
            ),
        ),
        MuscleMapRegionKey.CALVES to artwork(
            front = gendered(
                male = listOf("front_gastrocnemius_male.webp", "front_soleus_male.webp"),
                female = listOf("front_gastrocnemius_female.webp", "front_soleus_female.webp"),
            ),
            back = gendered(
                male = listOf("back_gastrocnemius_male.webp", "back_soleus_male.webp"),
                female = listOf("back_gastrocnemius_female.webp", "back_soleus_female.webp"),
            ),
        ),
        MuscleMapRegionKey.CHEST to artwork(
            front = gendered(
                male = listOf("front_chest_male.webp", "front_chest_sternal_head_male.webp"),
                female = listOf("front_chest_female.webp", "front_chest_sternal_head_female.webp"),
            ),
        ),
        MuscleMapRegionKey.FOREARMS to artwork(
            front = gendered(
                male = listOf("front_wrist_extensors_male.webp", "front_brachioradialis_male.webp"),
                female = listOf("front_wrist_extensors_female.webp", "front_brachioradialis_female.webp"),
            ),
            back = gendered(
                male = listOf("back_wrist_flexors_male.webp"),
                female = listOf("back_wrist_flexors_female.webp"),
            ),
        ),
        MuscleMapRegionKey.HAMSTRINGS to artwork(
            back = gendered(
                male = listOf("back_hamstrings_male.webp"),
                female = listOf("back_hamstrings_female.webp"),
            ),
        ),
        MuscleMapRegionKey.HIPS to artwork(
            front = gendered(
                male = listOf("front_hip_adductors_male.webp"),
                female = listOf("front_hip_adductors_female.webp"),
            ),
            back = gendered(
                male = listOf("back_gluteus_maximus_male.webp", "back_gluteus_medius_male.webp"),
                female = listOf("back_gluteus_maximus_female.webp", "back_gluteus_medius_female.webp"),
            ),
        ),
        MuscleMapRegionKey.NECK to artwork(
            front = gendered(
                male = listOf("front_neck_male.webp"),
                female = listOf("front_neck_female.webp"),
            ),
            back = gendered(
                male = listOf("back_neck_male.webp"),
                female = listOf("back_neck_female.webp"),
            ),
        ),
        MuscleMapRegionKey.QUADRICEPS to artwork(
            front = gendered(
                male = listOf("front_quadriceps_male.webp"),
                female = listOf("front_quadriceps_female.webp"),
            ),
            back = gendered(
                male = listOf("back_quadriceps_male.webp"),
                female = listOf("back_quadriceps_female.webp"),
            ),
        ),
        MuscleMapRegionKey.SHOULDERS to artwork(
            front = gendered(
                male = listOf("front_deltoid_anterior_male.webp", "front_deltoid_lateral_male.webp"),
                female = listOf("front_deltoid_anterior_female.webp", "front_deltoid_lateral_female.webp"),
            ),
            back = gendered(
                male = listOf("back_deltoids_male.webp"),
                female = listOf("back_deltoids_female.webp"),
            ),
        ),
        MuscleMapRegionKey.TRICEPS to artwork(
            front = gendered(
                male = listOf("front_triceps_male.webp"),
                female = listOf("front_triceps_female.webp"),
            ),
            back = gendered(
                male = listOf("back_triceps_male.webp"),
                female = listOf("back_triceps_female.webp"),
            ),
        ),
    )

    fun resolveRegion(rawMuscleGroup: String?): MuscleMapRegionKey? = rawMuscleGroup
        ?.normalizeMuscleMapKey()
        ?.takeIf(String::isNotEmpty)
        ?.let(regionsByAlias::get)

    fun resolve(
        rawMuscleGroups: Iterable<String>,
        variant: ExerciseMediaVariant,
    ): MuscleMapLayerSet? {
        if (variant == ExerciseMediaVariant.NEUTRAL) return null
        val regions = rawMuscleGroups.mapNotNull(::resolveRegion).distinct()
        if (regions.isEmpty()) return null

        val frontOverlays = regions
            .flatMap { region -> artworkByRegion[region]?.front?.assetsFor(variant).orEmpty() }
            .distinct()
        val backOverlays = regions
            .flatMap { region -> artworkByRegion[region]?.back?.assetsFor(variant).orEmpty() }
            .distinct()
        if (frontOverlays.isEmpty() && backOverlays.isEmpty()) return null

        return MuscleMapLayerSet(
            variant = variant,
            frontBaseAsset = when (variant) {
                ExerciseMediaVariant.MALE -> "front_grey_body_male.webp"
                ExerciseMediaVariant.FEMALE -> "front_grey_body_female.webp"
                ExerciseMediaVariant.NEUTRAL -> error("Neutral variants do not resolve artwork.")
            },
            backBaseAsset = when (variant) {
                ExerciseMediaVariant.MALE -> "back_body_male.webp"
                ExerciseMediaVariant.FEMALE -> "back_body_female.webp"
                ExerciseMediaVariant.NEUTRAL -> error("Neutral variants do not resolve artwork.")
            },
            frontOverlayAssets = frontOverlays,
            backOverlayAssets = backOverlays,
        )
    }
}

private data class GenderedAssets(
    val male: List<String>,
    val female: List<String>,
) {
    fun assetsFor(variant: ExerciseMediaVariant): List<String> = when (variant) {
        ExerciseMediaVariant.MALE -> male
        ExerciseMediaVariant.FEMALE -> female
        ExerciseMediaVariant.NEUTRAL -> emptyList()
    }
}

private data class RegionArtwork(
    val front: GenderedAssets?,
    val back: GenderedAssets?,
)

private fun artwork(
    front: GenderedAssets? = null,
    back: GenderedAssets? = null,
): RegionArtwork = RegionArtwork(front = front, back = back)

private fun gendered(
    male: List<String>,
    female: List<String>,
): GenderedAssets = GenderedAssets(male = male, female = female)

private val muscleMapSeparator = Regex("[^a-z0-9]+")

private fun String.normalizeMuscleMapKey(): String = trim()
    .lowercase(Locale.ROOT)
    .replace(muscleMapSeparator, "_")
    .trim('_')
