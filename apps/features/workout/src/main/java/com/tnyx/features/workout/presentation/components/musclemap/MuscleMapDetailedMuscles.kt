package com.tnyx.features.workout.presentation.components.musclemap

/**
 * Represents a single anatomical muscle entry for use in the muscle selection UI.
 *
 * @param id           Unique stable key used for asset resolution in [MuscleMapAssetRegistry].
 * @param displayName  Human-readable name shown in the UI list.
 * @param regionKey    Broad category key used as fallback for asset resolution.
 * @param defaultView  Which body side (FRONT/BACK) to show in the thumbnail avatar.
 */
data class DetailedMuscleItem(
    val id: String,
    val displayName: String,
    val regionKey: String,
    val defaultView: MuscleMapView = MuscleMapView.FRONT,
)

/**
 * Full 44-muscle anatomical list matching Lyfta 1.581 [Muscle.java] enum parity.
 * Source-of-truth for any muscle selection UI in the workout feature.
 *
 * Order follows Lyfta's populateMuscleMap() grouping:
 * neck → chest → shoulders → arms → core → back → hips → legs → calves
 */
val DetailedMuscleList: List<DetailedMuscleItem> = listOf(
    // ─── Neck ───────────────────────────────────────────────────────────────
    DetailedMuscleItem("sternocleidomastoid", "Sternocleidomastoid", "neck", MuscleMapView.FRONT),
    DetailedMuscleItem("levator_scapulae",    "Levator scapulae",    "neck", MuscleMapView.BACK),
    DetailedMuscleItem("splenius",            "Splenius",            "neck", MuscleMapView.BACK),

    // ─── Chest ──────────────────────────────────────────────────────────────
    DetailedMuscleItem("pectoralis_major_sternal_head",    "Pectoralis major",                  "chest", MuscleMapView.FRONT),
    DetailedMuscleItem("pectoralis_major_clavicular_head", "Pectoralis major clavicular head",  "chest", MuscleMapView.FRONT),
    DetailedMuscleItem("serratus_anterior",                "Serratus anterior",                 "chest", MuscleMapView.FRONT),

    // ─── Shoulders ──────────────────────────────────────────────────────────
    DetailedMuscleItem("deltoid_anterior",  "Anterior deltoid",  "shoulders", MuscleMapView.FRONT),
    DetailedMuscleItem("deltoid_lateral",   "Lateral deltoid",   "shoulders", MuscleMapView.FRONT),
    DetailedMuscleItem("deltoid_posterior", "Posterior deltoid", "shoulders", MuscleMapView.BACK),

    // ─── Biceps / Arms ──────────────────────────────────────────────────────
    DetailedMuscleItem("biceps_brachii", "Biceps brachii", "biceps",   MuscleMapView.FRONT),
    DetailedMuscleItem("brachialis",     "Brachialis",     "biceps",   MuscleMapView.FRONT),

    // ─── Triceps ────────────────────────────────────────────────────────────
    DetailedMuscleItem("triceps_brachii", "Triceps brachii", "triceps", MuscleMapView.BACK),

    // ─── Forearms ───────────────────────────────────────────────────────────
    DetailedMuscleItem("brachioradialis",  "Brachioradialis",  "forearms", MuscleMapView.FRONT),
    DetailedMuscleItem("wrist_extensors",  "Wrist extensors",  "forearms", MuscleMapView.FRONT),
    DetailedMuscleItem("wrist_flexors",    "Wrist flexors",    "forearms", MuscleMapView.BACK),

    // ─── Core / Abs ─────────────────────────────────────────────────────────
    DetailedMuscleItem("rectus_abdominis",    "Rectus abdominis",    "abs", MuscleMapView.FRONT),
    DetailedMuscleItem("obliques",            "Obliques",            "abs", MuscleMapView.FRONT),
    DetailedMuscleItem("transverse_abdominus","Transverse abdominis","abs", MuscleMapView.FRONT),

    // ─── Back ───────────────────────────────────────────────────────────────
    DetailedMuscleItem("trapezius_upper_fibers",  "Upper trapezius",   "back", MuscleMapView.BACK),
    DetailedMuscleItem("trapezius_middle_fibers", "Middle trapezius",  "back", MuscleMapView.BACK),
    DetailedMuscleItem("trapezius_lower_fibers",  "Lower trapezius",   "back", MuscleMapView.BACK),
    DetailedMuscleItem("infraspinatus",           "Infraspinatus",     "back", MuscleMapView.BACK),
    DetailedMuscleItem("teres_major",             "Teres major",       "back", MuscleMapView.BACK),
    DetailedMuscleItem("teres_minor",             "Teres minor",       "back", MuscleMapView.BACK),
    DetailedMuscleItem("subscapularis",           "Subscapularis",     "back", MuscleMapView.BACK),
    DetailedMuscleItem("latissimus_dorsi",        "Latissimus dorsi",  "back", MuscleMapView.BACK),
    DetailedMuscleItem("erector_spinae",          "Erector spinae",    "back", MuscleMapView.BACK),

    // ─── Hips ───────────────────────────────────────────────────────────────
    DetailedMuscleItem("iliopsoas",                  "Iliopsoas",                    "hips", MuscleMapView.FRONT),
    DetailedMuscleItem("pectineus",                  "Pectineus",                    "hips", MuscleMapView.FRONT),
    DetailedMuscleItem("tensor_fasciae_latae",       "Tensor fasciae latae",         "hips", MuscleMapView.FRONT),
    DetailedMuscleItem("adductor_longus",            "Adductor longus",              "hips", MuscleMapView.FRONT),
    DetailedMuscleItem("adductor_magnus",            "Adductor magnus",              "hips", MuscleMapView.BACK),
    DetailedMuscleItem("gracilis",                   "Gracilis",                     "hips", MuscleMapView.BACK),
    DetailedMuscleItem("gluteus_maximus",             "Gluteus maximus",              "hips", MuscleMapView.BACK),
    DetailedMuscleItem("gluteus_medius",              "Gluteus medius",               "hips", MuscleMapView.BACK),
    DetailedMuscleItem("gluteus_minimus",             "Gluteus minimus",              "hips", MuscleMapView.BACK),
    DetailedMuscleItem("deep_hip_external_rotators", "Deep hip external rotators",   "hips", MuscleMapView.BACK),

    // ─── Quadriceps / Hamstrings ─────────────────────────────────────────────
    DetailedMuscleItem("quadriceps", "Quadriceps", "quadriceps", MuscleMapView.FRONT),
    DetailedMuscleItem("sartorius",  "Sartorius",  "quadriceps", MuscleMapView.FRONT),
    DetailedMuscleItem("hamstrings", "Hamstrings", "hamstrings", MuscleMapView.BACK),
    DetailedMuscleItem("popliteus",  "Popliteus",  "hamstrings", MuscleMapView.BACK),

    // ─── Calves ─────────────────────────────────────────────────────────────
    DetailedMuscleItem("gastrocnemius",   "Gastrocnemius",   "calves", MuscleMapView.BACK),
    DetailedMuscleItem("tibialis_anterior","Tibialis anterior","calves", MuscleMapView.FRONT),
    DetailedMuscleItem("soleus",          "Soleus",          "calves", MuscleMapView.BACK),
)
