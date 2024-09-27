package com.example.platten.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.platten.data.ExerciseLog
import com.example.platten.ui.components.WeightProgressChart
import com.example.platten.viewmodel.ExerciseViewModel
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    navController: NavController,
    exerciseId: Int,
    viewModel: ExerciseViewModel = viewModel()
) {
    val exercise = viewModel.getExerciseById(exerciseId).collectAsState(initial = null)
    val logs = viewModel.getLogsForExercise(exerciseId).collectAsState(initial = emptyList())
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedLog by remember { mutableStateOf<ExerciseLog?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise.value?.name ?: "Exercise Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            exercise.value?.let { ex ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Weight") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = reps,
                            onValueChange = { reps = it },
                            label = { Text("Reps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                val weightValue = weight.toFloatOrNull()
                                val repsValue = reps.toIntOrNull()
                                if (weightValue != null && repsValue != null) {
                                    val log = ExerciseLog(
                                        exerciseId = ex.id,
                                        date = Date(),
                                        weight = weightValue,
                                        reps = repsValue
                                    )
                                    viewModel.insertLog(log)
                                    weight = ""
                                    reps = ""
                                }
                            }
                        ) {
                            Text("Log Exercise")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Text("Estimated 1RM Progress", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .padding(vertical = 8.dp)
                    ) {
                        WeightProgressChart(logs.value.map { Triple(it.exerciseId, it.weight, it.reps) })
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Text("Exercise History", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(logs.value.sortedByDescending { it.date }) { log ->
                    ExerciseLogItem(log) {
                        selectedLog = log
                        showEditDialog = true
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } ?: item { Text("Loading exercise details...") }
        }
    }

    if (showEditDialog) {
        EditExerciseLogDialog(
            log = selectedLog,
            onDismiss = { showEditDialog = false },
            onSave = { updatedLog ->
                viewModel.updateLog(updatedLog)
                showEditDialog = false
            },
            onDelete = { logToDelete ->
                viewModel.deleteLog(logToDelete)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun EditExerciseLogDialog(
    log: ExerciseLog?,
    onDismiss: () -> Unit,
    onSave: (ExerciseLog) -> Unit,
    onDelete: (ExerciseLog) -> Unit
) {
    if (log == null) return

    var weight by remember { mutableStateOf(log.weight.toString()) }
    var reps by remember { mutableStateOf(log.reps.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Exercise Log") },
        text = {
            Column {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedLog = log.copy(
                        weight = weight.toFloatOrNull() ?: log.weight,
                        reps = reps.toIntOrNull() ?: log.reps
                    )
                    onSave(updatedLog)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                TextButton(onClick = { onDelete(log) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

@Composable
fun ExerciseLogItem(log: ExerciseLog, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),  // Add clickable modifier
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = dateFormat.format(log.date), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Weight: ${log.weight} kg", style = MaterialTheme.typography.bodySmall)
                Text(text = "Reps: ${log.reps}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}