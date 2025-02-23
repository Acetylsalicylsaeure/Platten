package com.acetylsalicylsaeure.platten.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Transaction
    @Query("SELECT * FROM workouts ORDER BY lastViewed DESC")
    fun getAllWorkoutsWithExercises(): Flow<List<WorkoutWithExercises>>

    @Insert
    suspend fun insertWorkout(workout: Workout): Long

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Insert
    suspend fun insertWorkoutExerciseCrossRef(crossRef: WorkoutExerciseCrossRef)

    @Delete
    suspend fun deleteWorkoutExerciseCrossRef(crossRef: WorkoutExerciseCrossRef)

    @Query("DELETE FROM workout_exercise_cross_ref WHERE workoutId = :workoutId AND exerciseId = :exerciseId")
    suspend fun removeExerciseFromWorkout(workoutId: Int, exerciseId: Int)

    @Query("SELECT MAX(orderPosition) FROM workout_exercise_cross_ref WHERE workoutId = :workoutId")
    suspend fun getMaxOrderPosition(workoutId: Int): Int?

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    fun getWorkoutById(workoutId: Int): Flow<Workout?>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    fun getWorkoutWithExercisesById(workoutId: Int): Flow<WorkoutWithExercises?>

    @Transaction
    suspend fun deleteWorkoutWithExercises(workout: Workout) {
        deleteWorkout(workout)
        // Cross references will be deleted automatically due to CASCADE
    }
}