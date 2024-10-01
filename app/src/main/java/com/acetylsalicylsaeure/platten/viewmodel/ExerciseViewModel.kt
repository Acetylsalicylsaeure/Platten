package com.acetylsalicylsaeure.platten.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.acetylsalicylsaeure.platten.data.AppDatabase
import com.acetylsalicylsaeure.platten.data.Exercise
import com.acetylsalicylsaeure.platten.data.ExerciseLog
import com.acetylsalicylsaeure.platten.data.ExerciseRepository
import com.acetylsalicylsaeure.platten.data.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.lang.Math.exp
import java.util.Date
import kotlin.math.ln
import com.acetylsalicylsaeure.platten.ui.components.calculateEstimatedOneRM

class ExerciseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExerciseRepository
    private val preferences: Preferences
    val exercises: StateFlow<List<Exercise>>
    val sortedExercisesWithLastTrained: StateFlow<List<Pair<Exercise, Date?>>>

    val weightedRegressionFlow: Flow<Boolean>
    val regressionWindowFlow: Flow<Int>

    init {
        val database = AppDatabase.getDatabase(application)
        val exerciseDao = database.exerciseDao()
        val exerciseLogDao = database.exerciseLogDao()
        repository = ExerciseRepository(exerciseDao, exerciseLogDao)
        preferences = Preferences(application)

        weightedRegressionFlow = preferences.weightedRegressionFlow
        regressionWindowFlow = preferences.regressionWindowFlow

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

    fun updateLog(log: ExerciseLog) {
        viewModelScope.launch {
            repository.updateLog(log)
        }
    }

    fun deleteLog(log: ExerciseLog) {
        viewModelScope.launch {
            repository.deleteLog(log)
        }
    }

    // regression part


    fun calculateLinearRegression(logs: List<ExerciseLog>, weightedRegression: Boolean, regressionWindow: Int): Pair<Double, Double>? {
        if (logs.isEmpty()) return null

        val filteredLogs = if (regressionWindow > 0) {
            logs.takeLast(regressionWindow)
        } else {
            logs
        }

        val n = filteredLogs.size
        var sumX = 0.0
        var sumY = 0.0
        var sumXY = 0.0
        var sumX2 = 0.0
        var sumWeights = 0.0

        filteredLogs.forEachIndexed { index, log ->
            val x = index.toDouble()
            val y = calculateEstimatedOneRM(log.weight, log.reps)
            val weight = if (weightedRegression) (index + 1.0) / n else 1.0

            sumX += x * weight
            sumY += y * weight
            sumXY += x * y * weight
            sumX2 += x * x * weight
            sumWeights += weight
        }

        val slope = (sumWeights * sumXY - sumX * sumY) / (sumWeights * sumX2 - sumX * sumX)
        val intercept = (sumY - slope * sumX) / sumWeights

        return Pair(slope, intercept)
    }
}
