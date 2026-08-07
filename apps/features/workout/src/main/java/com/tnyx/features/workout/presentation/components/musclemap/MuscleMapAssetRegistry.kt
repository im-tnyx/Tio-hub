package com.tnyx.features.workout.presentation.components.musclemap

import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant

// MuscleMapView, MuscleMapRegionKey, MuscleMapLayerSet, and normalizeMuscleMapKey
// are defined in MuscleMapModels.kt (same package).

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

    private val detailedArtworkByAlias: Map<String, RegionArtwork> = mapOf(
        "sternocleidomastoid" to artwork(
            front = gendered("front_neck_male.webp", "front_neck_female.webp"),
            back = gendered("back_neck_male.webp", "back_neck_female.webp")
        ),
        "pectoralis_major" to artwork(
            front = gendered(listOf("front_chest_male.webp", "front_chest_sternal_head_male.webp"), listOf("front_chest_female.webp", "front_chest_sternal_head_female.webp"))
        ),
        "pectoralis_major_sternal_head" to artwork(
            front = gendered(listOf("front_chest_male.webp", "front_chest_sternal_head_male.webp"), listOf("front_chest_female.webp", "front_chest_sternal_head_female.webp"))
        ),
        "pectoralis_major_clavicular_head" to artwork(
            front = gendered(listOf("front_chest_male.webp", "front_chest_clavicular_head_male.webp"), listOf("front_chest_female.webp"))
        ),
        "deltoid_anterior" to artwork(
            front = gendered("front_deltoid_anterior_male.webp", "front_deltoid_anterior_female.webp")
        ),
        "anterior_deltoid" to artwork(
            front = gendered("front_deltoid_anterior_male.webp", "front_deltoid_anterior_female.webp")
        ),
        "deltoid_lateral" to artwork(
            front = gendered("front_deltoid_lateral_male.webp", "front_deltoid_lateral_female.webp")
        ),
        "lateral_deltoid" to artwork(
            front = gendered("front_deltoid_lateral_male.webp", "front_deltoid_lateral_female.webp")
        ),
        "deltoid_posterior" to artwork(
            back = gendered("back_deltoids_male.webp", "back_deltoid_female.webp")
        ),
        "posterior_deltoid" to artwork(
            back = gendered("back_deltoids_male.webp", "back_deltoid_female.webp")
        ),
        "brachioradialis" to artwork(
            front = gendered("front_brachioradialis_male.webp", "front_brachioradialis_female.webp")
        ),
        "rectus_abdominis" to artwork(
            front = gendered("front_rectus_abdominis_male.webp", "front_rectus_abdominis_female.webp")
        ),
        "sartorius" to artwork(
            front = gendered("front_sartorius_male.webp", "front_sartorius_female.webp")
        ),
        "serratus_anterior" to artwork(
            front = gendered("front_serratus_anterior_male.webp", "front_serratus_anterior_female.webp")
        ),
        "pectineus" to artwork(
            front = gendered("front_hip_adductors_male.webp", "front_hip_adductors_female.webp")
        ),
        "transverse_abdominus" to artwork(
            front = gendered("front_rectus_abdominis_male.webp", "front_rectus_abdominis_female.webp")
        ),
        "transverse_abdominis" to artwork(
            front = gendered("front_rectus_abdominis_male.webp", "front_rectus_abdominis_female.webp")
        ),
        "tensor_fasciae_latae" to artwork(
            front = gendered("front_tensor_fasciae_latae_male.webp", "front_tensor_fasciae_latae_female.webp")
        ),
        "iliopsoas" to artwork(
            front = gendered("front_iliopsoas_male.webp", "front_iliopsoas_female.webp")
        ),
        "wrist_extensors" to artwork(
            front = gendered("front_wrist_extensors_male.webp", "front_wrist_extensors_female.webp")
        ),
        "wrist_flexors" to artwork(
            back = gendered("back_wrist_flexors_male.webp", "back_wrist_flexors_female.webp")
        ),
        "trapezius_lower_fibers" to artwork(
            back = gendered("back_lower_trapezius_male.webp", "back_trapezius_female.webp")
        ),
        "lower_trapezius" to artwork(
            back = gendered("back_lower_trapezius_male.webp", "back_trapezius_female.webp")
        ),
        "trapezius_upper_fibers" to artwork(
            front = gendered("front_trapezius_male.webp", "front_trapezius_female.webp"),
            back = gendered("back_upper_trapezius_male.webp", "back_trapezius_female.webp")
        ),
        "upper_trapezius" to artwork(
            front = gendered("front_trapezius_male.webp", "front_trapezius_female.webp"),
            back = gendered("back_upper_trapezius_male.webp", "back_trapezius_female.webp")
        ),
        "trapezius_middle_fibers" to artwork(
            back = gendered(listOf("back_middle_trapezius_male.webp", "back_lower_trapezius_male.webp"), listOf("back_trapezius_female.webp"))
        ),
        "middle_trapezius" to artwork(
            back = gendered(listOf("back_middle_trapezius_male.webp", "back_lower_trapezius_male.webp"), listOf("back_trapezius_female.webp"))
        ),
        "infraspinatus" to artwork(
            back = gendered("back_infraspinatus_male.webp", "back_infraspinatus_female.webp")
        ),
        "teres_major" to artwork(
            back = gendered("back_teres_major_male.webp", "back_teres_major_female.webp")
        ),
        "teres_minor" to artwork(
            back = gendered("back_teres_minor_male.webp", "back_teres_minor_female.webp")
        ),
        "latissimus_dorsi" to artwork(
            back = gendered("back_latissimus_dorsi_male.webp", "back_latissimus_dorsi_female.webp")
        ),
        "erector_spinae" to artwork(
            back = gendered("back_erector_spinae_male.webp", "back_erector_spinae_female.webp")
        ),
        "adductor_longus" to artwork(
            front = gendered("front_hip_adductors_male.webp", "front_hip_adductors_female.webp")
        ),
        "adductor_magnus" to artwork(
            back = gendered("back_hip_adductors_male.webp", "back_hip_adductors_female.webp")
        ),
        "gluteus_maximus" to artwork(
            back = gendered("back_gluteus_maximus_male.webp", "back_gluteus_maximus_female.webp")
        ),
        "gluteus_medius" to artwork(
            back = gendered("back_gluteus_medius_male.webp", "back_gluteus_medius_female.webp")
        ),
        "gluteus_minimus" to artwork(
            back = gendered("back_gluteus_medius_male.webp", "back_gluteus_medius_female.webp")
        ),
        "deep_hip_external_rotators" to artwork(
            back = gendered("back_gluteus_medius_male.webp", "back_gluteus_medius_female.webp")
        ),
        "subscapularis" to artwork(
            back = gendered("back_infraspinatus_male.webp", "back_infraspinatus_female.webp")
        ),
        "hamstrings" to artwork(
            back = gendered("back_hamstrings_male.webp", "back_hamstrings_female.webp")
        ),
        "gracilis" to artwork(
            back = gendered("back_hip_adductors_male.webp", "back_hip_adductors_female.webp")
        ),
        "levator_scapulae" to artwork(
            back = gendered("back_neck_male.webp", "back_neck_female.webp")
        ),
        "popliteus" to artwork(
            back = gendered("back_popliteus_male.webp", "back_popliteus_female.webp")
        ),
        "splenius" to artwork(
            back = gendered("back_neck_male.webp", "back_neck_female.webp")
        ),
        "triceps_brachii" to artwork(
            front = gendered("front_triceps_male.webp", "front_triceps_female.webp"),
            back = gendered("back_triceps_male.webp", "back_triceps_female.webp")
        ),
        "biceps_brachii" to artwork(
            front = gendered("front_biceps_male.webp", "front_biceps_female.webp"),
            back = gendered("back_biceps_male.webp", "back_biceps_female.webp")
        ),
        "brachialis" to artwork(
            front = gendered("front_brachialis_male.webp", "front_brachialis_female.webp"),
            back = gendered("back_brachialis_male.webp", "back_brachialis_female.webp")
        ),
        "obliques" to artwork(
            front = gendered("front_obliques_male.webp", "front_obliques_female.webp"),
            back = gendered("back_obliques_male.webp", "back_obliques_female.webp")
        ),
        "quadriceps" to artwork(
            front = gendered("front_quadriceps_male.webp", "front_quadriceps_female.webp"),
            back = gendered("back_quadriceps_male.webp", "back_quadriceps_female.webp")
        ),
        "gastrocnemius" to artwork(
            front = gendered("front_gastrocnemius_male.webp", "front_gastrocnemius_female.webp"),
            back = gendered("back_gastrocnemius_male.webp", "back_gastrocnemius_female.webp")
        ),
        "tibialis_anterior" to artwork(
            front = gendered("front_tibias_male.webp", "front_tibias_female.webp")
        ),
        "soleus" to artwork(
            front = gendered("front_soleus_male.webp", "front_soleus_female.webp"),
            back = gendered("back_soleus_male.webp", "back_soleus_female.webp")
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

        val frontOverlays = mutableListOf<String>()
        val backOverlays = mutableListOf<String>()

        for (rawGroup in rawMuscleGroups) {
            val key = rawGroup.normalizeMuscleMapKey()
            val detailedArt = detailedArtworkByAlias[key]
            if (detailedArt != null) {
                frontOverlays.addAll(detailedArt.front?.assetsFor(variant).orEmpty())
                backOverlays.addAll(detailedArt.back?.assetsFor(variant).orEmpty())
            } else {
                val region = resolveRegion(rawGroup)
                if (region != null) {
                    val regionArt = artworkByRegion[region]
                    frontOverlays.addAll(regionArt?.front?.assetsFor(variant).orEmpty())
                    backOverlays.addAll(regionArt?.back?.assetsFor(variant).orEmpty())
                }
            }
        }

        val uniqueFront = frontOverlays.distinct()
        val uniqueBack = backOverlays.distinct()

        if (uniqueFront.isEmpty() && uniqueBack.isEmpty()) return null

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
            frontOverlayAssets = uniqueFront,
            backOverlayAssets = uniqueBack,
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
    maleAsset: String,
    femaleAsset: String,
): GenderedAssets = GenderedAssets(male = listOf(maleAsset), female = listOf(femaleAsset))

private fun gendered(
    male: List<String>,
    female: List<String>,
): GenderedAssets = GenderedAssets(male = male, female = female)

