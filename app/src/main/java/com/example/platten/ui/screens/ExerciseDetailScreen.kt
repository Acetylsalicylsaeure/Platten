package com.example.platten.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(0.7f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            WeightInputWithButtons(
                                weight = weight,
                                onWeightChange = { weight = it },
                                weightStep = ex.weightSteps
                            )
                            RepsInputWithButtons(
                                reps = reps,
                                onRepsChange = { reps = it }
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
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
                            },
                            modifier = Modifier
                                .weight(0.3f)
                                .fillMaxHeight()
                                .padding(top = 8.dp)
                        ) {
                            Text("Log\nExercise", textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Text("Estimated 1RM Progress", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightInputWithButtons(
    weight: String,
    onWeightChange: (String) -> Unit,
    weightStep: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalIconButton(
            onClick = {
                val currentWeight = weight.toFloatOrNull() ?: 0f
                onWeightChange((currentWeight - weightStep.toFloat()).coerceAtLeast(0f).toString())
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease Weight")
        }
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = weight,
            onValueChange = onWeightChange,
            label = { Text("Weight") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .weight(1f)
                .width(IntrinsicSize.Min),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
        )
        Spacer(modifier = Modifier.width(8.dp))
        FilledTonalIconButton(
            onClick = {
                val currentWeight = weight.toFloatOrNull() ?: 0f
                onWeightChange((currentWeight + weightStep.toFloat()).toString())
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase Weight")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepsInputWithButtons(
    reps: String,
    onRepsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalIconButton(
            onClick = {
                val currentReps = reps.toIntOrNull() ?: 0
                onRepsChange((currentReps - 1).coerceAtLeast(0).toString())
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease Reps")
        }
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = reps,
            onValueChange = onRepsChange,
            label = { Text("Reps") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .weight(1f)
                .width(IntrinsicSize.Min),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        FilledTonalIconButton(
            onClick = {
                val currentReps = reps.toIntOrNull() ?: 0
                onRepsChange((currentReps + 1).toString())
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase Reps")
        }
    }
}


@Composable
fun ExerciseLogItem(log: ExerciseLog) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
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