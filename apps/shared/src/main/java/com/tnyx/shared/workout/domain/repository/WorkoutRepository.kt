package com.tnyx.shared.workout.domain.repository

import com.tnyx.shared.workout.domain.model.WorkoutRoutine
import com.tnyx.shared.workout.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

/**
 * Workout Repository Interface — Shared
 *
 * यह interface :shared module में define होगा।
 * Implementation अलग-अलग होगी:
 *   - Phone App  → Room DB implementation
 *   - Watch App  → Room DB (watch-side cache) implementation
 *
 * KMP के बाद: commonMain में यही interface रहेगा।
 */
interface WorkoutRepository {

    /** All routines — synced from server / local cache */
    fun getRoutines(): Flow<List<WorkoutRoutine>>

    /** Single routine by id */
    suspend fun getRoutineById(id: String): WorkoutRoutine?

    /** Active session (if workout is ongoing) */
    fun getActiveSession(): Flow<WorkoutSession?>

    /** Start a new workout session */
    suspend fun startSession(routineId: String?): WorkoutSession

    /** Log a completed set */
    suspend fun logSet(sessionId: String, set: com.tnyx.shared.workout.domain.model.WorkoutSet)

    /** End the active workout session */
    suspend fun endSession(sessionId: String): WorkoutSession

    /** All past workout sessions */
    fun getSessionHistory(): Flow<List<WorkoutSession>>
}
