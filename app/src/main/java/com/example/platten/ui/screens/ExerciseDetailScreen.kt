package com.example.platten.ui.screens

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
                    Text("Name: ${ex.name}", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))

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
                    Text("Weight Steps: ${ex.weightSteps}", style = MaterialTheme.typography.bodyLarge)

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Weight Progress Chart
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
                    ExerciseLogItem(log)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } ?: item { Text("Loading exercise details...") }
        }
    }
}

@Composable
fun ExerciseLogItem(log: ExerciseLog) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
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