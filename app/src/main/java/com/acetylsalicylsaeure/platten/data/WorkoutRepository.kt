package com.acetylsalicylsaeure.platten.data

import kotlinx.coroutines.flow.Flow
import java.util.Date

class WorkoutRepository(private val workoutDao: WorkoutDao) {
    val allWorkoutsWithExercises: Flow<List<WorkoutWithExercises>> = workoutDao.getAllWorkoutsWithExercises()

    suspend fun createWorkout(name: String): Long {
        val workout = Workout(name = name, lastViewed = Date())
        return workoutDao.insertWorkout(workout)
    }

    suspend fun deleteWorkout(workout: Workout) {
        workoutDao.deleteWorkoutWithExercises(workout)
    }

    suspend fun addExerciseToWorkout(workoutId: Int, exerciseId: Int) {
        val maxPosition = workoutDao.getMaxOrderPosition(workoutId) ?: -1
        val nextPosition = maxPosition + 1

        val crossRef = WorkoutExerciseCrossRef(
            workoutId = workoutId,
            exerciseId = exerciseId,
            orderPosition = nextPosition
        )
        workoutDao.insertWorkoutExerciseCrossRef(crossRef)
    }

    suspend fun removeExerciseFromWorkout(workoutId: Int, exerciseId: Int) {
        workoutDao.removeExerciseFromWorkout(workoutId, exerciseId)
    }

    suspend fun updateWorkoutLastViewed(workout: Workout) {
        workoutDao.updateWorkout(workout.copy(lastViewed = Date()))
    }

    fun getWorkoutWithExercises(workoutId: Int): Flow<WorkoutWithExercises?> {
        return workoutDao.getWorkoutWithExercisesById(workoutId)
    }
}