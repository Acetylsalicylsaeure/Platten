package com.acetylsalicylsaeure.platten.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.acetylsalicylsaeure.platten.data.Workout
import com.acetylsalicylsaeure.platten.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutScreen(navController: NavController, viewModel: WorkoutViewModel = viewModel()) {
    val workouts by viewModel.workouts.collectAsState()
    var showAddWorkoutDialog by remember { mutableStateOf(false) }
    var selectedWorkout by remember { mutableStateOf<Workout?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (workouts.isEmpty()) {
            Text(
                text = "Click the + button to create a workout!",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(workouts) { workoutWithExercises ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .combinedClickable(
                                onClick = { navController.navigate("workout/${workoutWithExercises.workout.id}") },
                                onLongClick = {
                                    selectedWorkout = workoutWithExercises.workout
                                    showEditDialog = true
                                }
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = workoutWithExercises.workout.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatLastViewed(workoutWithExercises.workout.lastViewed),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${workoutWithExercises.exercises.size} exercises",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddWorkoutDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Workout")
        }
    }

    if (showAddWorkoutDialog) {
        AddWorkoutDialog(
            onDismiss = { showAddWorkoutDialog = false },
            onConfirm = { name ->
                viewModel.createWorkout(name)
                showAddWorkoutDialog = false
            }
        )
    }

    if (showEditDialog && selectedWorkout != null) {
        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
                selectedWorkout = null
            },
            title = { Text("Edit Workout") },
            text = {
                var name by remember { mutableStateOf(selectedWorkout!!.name) }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Workout Name") }
                )
            },
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            selectedWorkout?.let { workout ->
                                viewModel.deleteWorkout(workout)
                            }
                            showEditDialog = false
                            selectedWorkout = null
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                    TextButton(
                        onClick = {
                            selectedWorkout?.let { workout ->
                                viewModel.renameWorkout(workout.id, workout.name)
                            }
                            showEditDialog = false
                            selectedWorkout = null
                        }
                    ) {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        selectedWorkout = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddWorkoutDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var workoutName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Workout") },
        text = {
            OutlinedTextField(
                value = workoutName,
                onValueChange = { workoutName = it },
                label = { Text("Workout Name") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(workoutName) },
                enabled = workoutName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatLastViewed(date: Date): String {
    val now = Calendar.getInstance()
    val lastViewed = Calendar.getInstance().apply { time = date }

    return when {
        now.get(Calendar.DATE) == lastViewed.get(Calendar.DATE) -> {
            SimpleDateFormat("'Today at' HH:mm", Locale.getDefault()).format(date)
        }
        now.get(Calendar.DATE) - lastViewed.get(Calendar.DATE) == 1 -> {
            SimpleDateFormat("'Yesterday at' HH:mm", Locale.getDefault()).format(date)
        }
        now.get(Calendar.YEAR) == lastViewed.get(Calendar.YEAR) -> {
            SimpleDateFormat("MMM d 'at' HH:mm", Locale.getDefault()).format(date)
        }
        else -> {
            SimpleDateFormat("MMM d, yyyy 'at' HH:mm", Locale.getDefault()).format(date)
        }
    }
}