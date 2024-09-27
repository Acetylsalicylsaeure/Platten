package com.example.platten.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.platten.data.AppDatabase
import com.example.platten.data.Exercise
import com.example.platten.data.ExerciseLog
import com.example.platten.data.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Date

class ExerciseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExerciseRepository
    val exercises: StateFlow<List<Exercise>>
    val sortedExercisesWithLastTrained: StateFlow<List<Pair<Exercise, Date?>>>

    init {
        val database = AppDatabase.getDatabase(application)
        val exerciseDao = database.exerciseDao()
        val exerciseLogDao = database.exerciseLogDao()
        repository = ExerciseRepository(exerciseDao, exerciseLogDao)
        exercises = repository.allExercises.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        sortedExercisesWithLastTrained = combine(
            exercises,
            repository.getLastTrainedDates()
        ) { exerciseList, lastTrainedDates ->
            exerciseList.map { exercise ->
                exercise to lastTrainedDates[exercise.id]
            }.sortedWith(compareBy<Pair<Exercise, Date?>> { it.second }.thenBy { it.first.name })
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun addExercise(name: String, weightSteps: Double) {
        viewModelScope.launch {
            val newExercise = Exercise(
                id = 0, // Room will auto-generate the ID
                name = name,
                weightSteps = weightSteps
            )
            repository.insertExercise(newExercise)
        }
    }

    fun updateExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.updateExercise(exercise)
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }

    fun getExerciseById(id: Int): Flow<Exercise?> {
        return repository.getExerciseById(id)
    }

    fun insertLog(log: ExerciseLog) {
        viewModelScope.launch {
            repository.insertLog(log)
        }
    }

    fun getLogsForExercise(exerciseId: Int): Flow<List<ExerciseLog>> {
        return repository.getLogsForExercise(exerciseId)
    }

    fun getLastTrainedDates(): Flow<Map<Int, Date?>> {
        return repository.getLastTrainedDates()
    }
}
