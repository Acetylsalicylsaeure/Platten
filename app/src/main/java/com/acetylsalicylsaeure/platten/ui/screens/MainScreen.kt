package com.acetylsalicylsaeure.platten.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.acetylsalicylsaeure.platten.data.Exercise
import com.acetylsalicylsaeure.platten.ui.components.ExerciseItem
import com.acetylsalicylsaeure.platten.viewmodel.ExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: ExerciseViewModel = viewModel()) {
    val sortedExercisesWithLastTrained by viewModel.sortedExercisesWithLastTrained.collectAsState()
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercises") },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
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
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (sortedExercisesWithLastTrained.isEmpty()) {
                Text(
                    text = "Click the + button to add an exercise!",
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
                    items(sortedExercisesWithLastTrained) { (exercise, lastTrainedDate) ->
                        ExerciseItem(
                            exercise = exercise,
                            lastTrainedDate = lastTrainedDate,
                            onClick = { navController.navigate("exercise/${exercise.id}") },
                            onLongClick = {
                                selectedExercise = exercise
                                showEditDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddExerciseDialog) {
        AddExerciseDialog(
            onDismiss = { showAddExerciseDialog = false },
            onConfirm = { name, weightSteps ->
                viewModel.addExercise(name, weightSteps)
                showAddExerciseDialog = false
            }
        )
    }

    if (showEditDialog) {
        EditExerciseDialog(
            exercise = selectedExercise,
            onDismiss = { showEditDialog = false },
            onSave = { updatedExercise ->
                viewModel.updateExercise(updatedExercise)
                showEditDialog = false
            },
            onDelete = {
                selectedExercise?.let { exercise ->
                    viewModel.deleteExercise(exercise)
                    showEditDialog = false
                }
            }
        )
    }
}


@Composable
fun AddExerciseDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var exerciseName by remember { mutableStateOf("") }
    var weightSteps by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Exercise") },
        text = {
            Column {
                TextField(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    label = { Text("Exercise Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = weightSteps,
                    onValueChange = { weightSteps = it },
                    label = { Text("Weight Steps") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val steps = weightSteps.toDoubleOrNull() ?: 0.0
                    onConfirm(exerciseName, steps)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExerciseDialog(
    exercise: Exercise?,
    onDismiss: () -> Unit,
    onSave: (Exercise) -> Unit,
    onDelete: () -> Unit
) {
    if (exercise == null) return

    var name by remember { mutableStateOf(exercise.name) }
    var weightSteps by remember { mutableStateOf(exercise.weightSteps.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                // Top app bar with back arrow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        "Edit Exercise",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Exercise name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Weight steps input
                OutlinedTextField(
                    value = weightSteps,
                    onValueChange = { weightSteps = it },
                    label = { Text("Weight Steps") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                    Button(
                        onClick = {
                            onSave(exercise.copy(
                                name = name,
                                weightSteps = weightSteps.toDoubleOrNull() ?: exercise.weightSteps
                            ))
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
