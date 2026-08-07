package com.tnyx.features.workout.presentation.components.bodypart

import androidx.annotation.DrawableRes
import com.tnyx.features.workout.R
import java.util.Locale

enum class BodyPartIconKey(
    @DrawableRes val drawableRes: Int,
    vararg aliases: String,
) {
    ABS(R.drawable.ic_body_part_abs, "abs", "abdominals", "core", "waist"),
    BACK(R.drawable.ic_body_part_back, "back", "body_back"),
    BICEPS(R.drawable.ic_body_part_biceps, "bicep", "biceps"),
    CALVES(R.drawable.ic_body_part_calves, "calf", "calves"),
    CARDIO(R.drawable.ic_body_part_cardio, "cardio"),
    CHEST(R.drawable.ic_body_part_chest, "chest", "pectorals", "pecs"),
    FOREARMS(R.drawable.ic_body_part_forearms, "forearm", "forearms"),
    HAMSTRINGS(R.drawable.ic_body_part_hamstrings, "hamstring", "hamstrings"),
    HIPS(R.drawable.ic_body_part_hips, "hip", "hips"),
    NECK(R.drawable.ic_body_part_neck, "neck"),
    QUADRICEPS(R.drawable.ic_body_part_quadriceps, "quadriceps", "quad", "quads"),
    SHOULDERS(R.drawable.ic_body_part_shoulders, "shoulder", "shoulders", "deltoids"),
    STRETCHING(R.drawable.ic_body_part_stretching, "stretch", "stretching"),
    TRICEPS(R.drawable.ic_body_part_triceps, "tricep", "triceps"),
    ;

    internal val normalizedAliases: Set<String> = aliases
        .mapTo(mutableSetOf(), String::normalizeBodyPartKey)
        .plus(name.normalizeBodyPartKey())
}

object BodyPartIconRegistry {
    private val entriesByAlias: Map<String, BodyPartIconKey> = buildMap {
        BodyPartIconKey.entries.forEach { entry ->
            entry.normalizedAliases.forEach { alias -> put(alias, entry) }
        }
    }

    fun resolve(rawBodyPart: String?): BodyPartIconKey? = rawBodyPart
        ?.normalizeBodyPartKey()
        ?.takeIf(String::isNotEmpty)
        ?.let(entriesByAlias::get)

    @DrawableRes
    fun drawableFor(rawBodyPart: String?): Int =
        resolve(rawBodyPart)?.drawableRes ?: R.drawable.tio_body_part_placeholder
}

private val bodyPartSeparator = Regex("[^a-z0-9]+")

private fun String.normalizeBodyPartKey(): String = trim()
    .lowercase(Locale.ROOT)
    .replace(bodyPartSeparator, "_")
    .trim('_')

/**
 * Maps a [BodyPartIconKey] to the corresponding muscle [regionKey] used in
 * [com.tnyx.features.workout.presentation.components.musclemap.DetailedMuscleItem].
 * Returns null for body parts that have no direct muscle mapping (CARDIO, STRETCHING).
 */
fun BodyPartIconKey.toMuscleRegionKey(): String? = when (this) {
    BodyPartIconKey.ABS         -> "abs"
    BodyPartIconKey.BACK        -> "back"
    BodyPartIconKey.BICEPS      -> "biceps"
    BodyPartIconKey.CALVES      -> "calves"
    BodyPartIconKey.CHEST       -> "chest"
    BodyPartIconKey.FOREARMS    -> "forearms"
    BodyPartIconKey.HAMSTRINGS  -> "hamstrings"
    BodyPartIconKey.HIPS        -> "hips"
    BodyPartIconKey.NECK        -> "neck"
    BodyPartIconKey.QUADRICEPS  -> "quadriceps"
    BodyPartIconKey.SHOULDERS   -> "shoulders"
    BodyPartIconKey.TRICEPS     -> "triceps"
    BodyPartIconKey.CARDIO,
    BodyPartIconKey.STRETCHING  -> null  // no anatomical muscle filter
}
