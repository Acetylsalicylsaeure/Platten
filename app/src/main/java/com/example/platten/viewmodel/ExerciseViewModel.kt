package com.example.platten.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.platten.data.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExerciseViewModel : ViewModel() {
    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises

    init {
        // TODO: Replace with actual data loading
        viewModelScope.launch {
            _exercises.value = listOf(
                Exercise(1, "Bench Press", 2.5),
                Exercise(2, "Squat", 2.5),
                Exercise(3, "Deadlift", 2.5)
            )
        }
    }
}
