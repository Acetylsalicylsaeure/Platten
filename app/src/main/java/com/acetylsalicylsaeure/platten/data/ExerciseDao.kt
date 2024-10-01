package com.acetylsalicylsaeure.platten.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    fun getExerciseById(id: Int): Flow<Exercise?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Query("UPDATE exercises SET weight_steps = :weightSteps WHERE id = :id")
    suspend fun updateExerciseWeightSteps(id: Int, weightSteps: Float)
}

