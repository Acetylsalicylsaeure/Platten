package com.acetylsalicylsaeure.platten.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.acetylsalicylsaeure.platten.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WorkoutRepository
    private val exerciseRepository: ExerciseRepository
    val workouts: StateFlow<List<WorkoutWithExercises>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = WorkoutRepository(database.workoutDao())
        exerciseRepository = ExerciseRepository(database.exerciseDao(), database.exerciseLogDao())
        workouts = repository.allWorkoutsWithExercises.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun createWorkout(name: String) {
        viewModelScope.launch {
            repository.createWorkout(name)
        }
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
        }
    }

    fun updateLastViewed(workout: Workout) {
        viewModelScope.launch {
            repository.updateWorkoutLastViewed(workout)
        }
    }

    fun getWorkoutWithExercises(workoutId: Int): Flow<WorkoutWithExercises?> {
        return repository.getWorkoutWithExercises(workoutId)
    }

    fun addExerciseToWorkout(workoutId: Int, exerciseId: Int) {
        viewModelScope.launch {
            repository.addExerciseToWorkout(workoutId, exerciseId)
        }
    }

    fun removeExerciseFromWorkout(workoutId: Int, exerciseId: Int) {
        viewModelScope.launch {
            repository.removeExerciseFromWorkout(workoutId, exerciseId)
        }
    }

    fun renameWorkout(workoutId: Int, newName: String) {
        viewModelScope.launch {
            repository.renameWorkout(workoutId, newName)
        }
    }

    fun getAllExercises(): Flow<List<Exercise>> {
        return exerciseRepository.allExercises
    }

    fun getLastTrainedDates(): Flow<Map<Int, Date?>> {
        return exerciseRepository.getLastTrainedDates()
    }
}