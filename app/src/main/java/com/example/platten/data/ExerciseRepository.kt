package com.example.platten.data

import kotlinx.coroutines.flow.Flow

class ExerciseRepository(
    private val exerciseDao: ExerciseDao,
    private val exerciseLogDao: ExerciseLogDao
) {
    val allExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()

    suspend fun insertExercise(exercise: Exercise) {
        exerciseDao.insertExercise(exercise)
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

    suspend fun deleteLog(log: ExerciseLog) {
        exerciseLogDao.deleteLog(log)
    }
}
