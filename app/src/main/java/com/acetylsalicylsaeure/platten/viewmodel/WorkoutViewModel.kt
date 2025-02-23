package com.acetylsalicylsaeure.platten.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.acetylsalicylsaeure.platten.data.AppDatabase
import com.acetylsalicylsaeure.platten.data.Workout
import com.acetylsalicylsaeure.platten.data.WorkoutRepository
import com.acetylsalicylsaeure.platten.data.WorkoutWithExercises
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WorkoutRepository
    val workouts: StateFlow<List<WorkoutWithExercises>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = WorkoutRepository(database.workoutDao())
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
}