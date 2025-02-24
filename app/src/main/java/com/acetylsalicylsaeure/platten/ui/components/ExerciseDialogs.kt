package com.acetylsalicylsaeure.platten.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.acetylsalicylsaeure.platten.data.Exercise

@Composable
fun AddExerciseDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var exerciseName by remember { mutableStateOf("") }
    var weightSteps by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Exercise") },
        text = {
            Column {
                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    label = { Text("Exercise Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = weightSteps,
                    onValueChange = { weightSteps = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
    onDelete: () -> Unit,
    onHide: (Int) -> Unit
) {
    if (exercise == null) return

    var name by remember { mutableStateOf(exercise.name) }
    var weightSteps by remember { mutableStateOf(exercise.weightSteps.toString()) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Button(
                            onClick = { showDeleteConfirmation = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onHide(exercise.id)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text("Hide")
                        }
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

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this exercise? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}