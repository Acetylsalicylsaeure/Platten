package com.example.platten.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    // Updated to use camelCase in Kotlin, while keeping snake_case in SQL
    @Query("UPDATE exercises SET weight_steps = :weightSteps WHERE id = :id")
    suspend fun updateExerciseWeightSteps(id: Int, weightSteps: Float)
}
