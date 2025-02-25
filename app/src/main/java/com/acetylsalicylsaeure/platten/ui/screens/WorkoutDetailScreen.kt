package com.acetylsalicylsaeure.platten.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.acetylsalicylsaeure.platten.data.Exercise
import com.acetylsalicylsaeure.platten.navigation.NavigationState
import com.acetylsalicylsaeure.platten.ui.components.ExerciseItem
import com.acetylsalicylsaeure.platten.viewmodel.WorkoutViewModel
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    navController: NavController,
    workoutId: Int,
    navigationState: NavigationState,
    onBackPressed: () -> Unit,
    viewModel: WorkoutViewModel = viewModel()
) {
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    // Update lastViewed directly based on workoutId
    LaunchedEffect(workoutId) {
        viewModel.updateWorkoutTimestamp(workoutId)
    }

    val workoutWithExercises by viewModel.getWorkoutWithExercises(workoutId).collectAsState(initial = null)
    val availableExercises by viewModel.getAllExercises().collectAsState(initial = emptyList())
    val lastTrainedDates by viewModel.getLastTrainedDates().collectAsState(initial = emptyMap())

    // Sort exercises by last trained date
    val sortedExercises = remember(workoutWithExercises, lastTrainedDates) {
        workoutWithExercises?.exercises?.sortedWith(
            compareBy<Exercise> { exercise ->
                lastTrainedDates[exercise.id]
            }.thenBy { it.name }
        ) ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workoutWithExercises?.workout?.name ?: "Workout Details") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddExerciseDialog = true }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Exercise")
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            if (sortedExercises.isEmpty()) {
                Text(
                    text = "Add exercises to your workout using the + button",
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sortedExercises) { exercise ->
                        ExerciseItem(
                            exercise = exercise,
                            lastTrainedDate = lastTrainedDates[exercise.id],
                            onClick = {
                                // Save workout as the source before navigating to exercise detail
                                navigationState.onScreenChanged("workout/$workoutId")
                                navController.navigate("exercise/${exercise.id}")
                            },
                            onLongClick = {
                                selectedExercise = exercise
                                showRemoveDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddExerciseDialog) {
        AddExerciseToWorkoutDialog(
            availableExercises = availableExercises.filter { exercise ->
                workoutWithExercises?.exercises?.none { it.id == exercise.id } ?: true
            },
            onDismiss = { showAddExerciseDialog = false },
            onExerciseSelected = { exercise ->
                viewModel.addExerciseToWorkout(workoutId, exercise.id)
                showAddExerciseDialog = false
            }
        )
    }

    if (showRemoveDialog && selectedExercise != null) {
        AlertDialog(
            onDismissRequest = {
                showRemoveDialog = false
                selectedExercise = null
            },
            title = { Text("Remove Exercise") },
            text = { Text("Remove ${selectedExercise?.name} from this workout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedExercise?.let { exercise ->
                            viewModel.removeExerciseFromWorkout(workoutId, exercise.id)
                        }
                        showRemoveDialog = false
                        selectedExercise = null
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRemoveDialog = false
                        selectedExercise = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseToWorkoutDialog(
    availableExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onExerciseSelected: (Exercise) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Exercise to Workout") },
        text = {
            LazyColumn {
                items(availableExercises) { exercise ->
                    ListItem(
                        headlineContent = { Text(exercise.name) },
                        modifier = Modifier.clickable {
                            onExerciseSelected(exercise)
                        }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}