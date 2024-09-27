package com.example.platten.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ExerciseLogDao {
    @Query("SELECT * FROM exercise_logs WHERE exerciseId = :exerciseId")
    fun getLogsForExercise(exerciseId: Int): Flow<List<ExerciseLog>>

    @Insert
    suspend fun insertLog(log: ExerciseLog)

    @Delete
    suspend fun deleteLog(log: ExerciseLog)

    @Query("SELECT exerciseId, MAX(date) as lastTrainedDate FROM exercise_logs GROUP BY exerciseId")
    fun getLastTrainedDates(): Flow<List<LastTrainedDate>>
}

data class LastTrainedDate(
    val exerciseId: Int,
    val lastTrainedDate: Date?
)
