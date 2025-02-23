package com.acetylsalicylsaeure.platten.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

class ExerciseRepository(
    private val exerciseDao: ExerciseDao,
    private val exerciseLogDao: ExerciseLogDao
) {
    val visibleExercises: Flow<List<Exercise>> = exerciseDao.getVisibleExercises()
    val allExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()

    suspend fun insertExercise(exercise: Exercise): Long {
        return exerciseDao.insertExercise(exercise)
    }

    suspend fun updateExercise(exercise: Exercise) {
        exerciseDao.updateExercise(exercise)
    }

    suspend fun deleteExercise(exercise: Exercise) {
        exerciseDao.deleteExercise(exercise)
    }

    fun getExerciseById(id: Int): Flow<Exercise?> {
        return exerciseDao.getExerciseById(id)
    }

    fun getLogsForExercise(exerciseId: Int): Flow<List<ExerciseLog>> {
        return exerciseLogDao.getLogsForExercise(exerciseId)
    }

    suspend fun insertLog(log: ExerciseLog) {
        exerciseLogDao.insertLog(log)
    }

    suspend fun updateLog(log: ExerciseLog) {
        exerciseLogDao.updateLog(log)
    }

    suspend fun deleteLog(log: ExerciseLog) {
        exerciseLogDao.deleteLog(log)
    }

    fun getLastTrainedDates(): Flow<Map<Int, Date?>> {
        return exerciseLogDao.getLastTrainedDates().map { logs ->
            logs.associate { it.exerciseId to it.lastTrainedDate }
        }
    }

    suspend fun setExerciseHidden(id: Int, hidden: Boolean) {
        exerciseDao.updateExerciseHidden(id, hidden)
    }
}
