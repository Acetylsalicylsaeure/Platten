package com.acetylsalicylsaeure.platten.ui.screens

import androidx.compose.foundation.clickable
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

@Composable
fun WorkoutsScreen(navController: NavController, viewModel: WorkoutViewModel = viewModel()) {
    val workouts by viewModel.workouts.collectAsState()
    var showAddWorkoutDialog by remember { mutableStateOf(false) }

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
                    WorkoutItem(
                        workout = workoutWithExercises.workout,
                        exerciseCount = workoutWithExercises.exercises.size,
                        onClick = {
                            viewModel.updateLastViewed(workoutWithExercises.workout)
                            // TODO: Navigate to workout detail screen
                            // navController.navigate("workout/${workoutWithExercises.workout.id}")
                        }
                    )
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
}

@Composable
fun WorkoutItem(
    workout: Workout,
    exerciseCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Text(
                text = workout.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatLastViewed(workout.lastViewed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$exerciseCount exercises",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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

fun formatLastViewed(date: Date): String {
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