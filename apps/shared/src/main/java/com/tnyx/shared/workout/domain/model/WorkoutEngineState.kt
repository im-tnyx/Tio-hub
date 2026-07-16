package com.tnyx.shared.workout.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class RestTimerStatus {
    IDLE,
    RUNNING,
    COMPLETED
}

@Serializable
data class RestTimerState(
    val status: RestTimerStatus = RestTimerStatus.IDLE,
    val exerciseEntryId: String? = null,
    val setId: String? = null,
    val durationSeconds: Int = 0,
    val startedAtMs: Long? = null,
    val endsAtMs: Long? = null
) {
    fun remainingSeconds(atTimeMs: Long): Int {
        if (status != RestTimerStatus.RUNNING || endsAtMs == null) return 0
        val safeTimeMs = atTimeMs.coerceAtLeast(0L)
        if (safeTimeMs >= endsAtMs) return 0
        val remainingMs = endsAtMs - safeTimeMs
        val wholeSeconds = remainingMs / 1_000L
        val roundedSeconds = wholeSeconds + if (remainingMs % 1_000L == 0L) 0L else 1L
        return roundedSeconds.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }
}

@Serializable
data class WorkoutEngineState(
    val schemaVersion: Int = WORKOUT_CONTRACT_VERSION,
    val session: WorkoutSession? = null,
    val restTimer: RestTimerState = RestTimerState()
)
