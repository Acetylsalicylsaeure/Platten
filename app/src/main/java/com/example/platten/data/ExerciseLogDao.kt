package com.example.platten.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseLogDao {
    @Query("SELECT * FROM exercise_logs WHERE exerciseId = :exerciseId")
    fun getLogsForExercise(exerciseId: Int): Flow<List<ExerciseLog>>

    @Insert
    suspend fun insertLog(log: ExerciseLog)

    @Delete
    suspend fun deleteLog(log: ExerciseLog)
}
