package com.acetylsalicylsaeure.platten.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.acetylsalicylsaeure.platten.data.ExerciseLog
import com.acetylsalicylsaeure.platten.data.Preferences
import com.acetylsalicylsaeure.platten.ui.components.Quadruple
import com.acetylsalicylsaeure.platten.ui.components.WeightProgressChart
import com.acetylsalicylsaeure.platten.ui.components.adjustWeightForReps
import com.acetylsalicylsaeure.platten.ui.components.roundToNearestWeightStep
import com.acetylsalicylsaeure.platten.viewmodel.ExerciseViewModel
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    navController: NavController,
    exerciseId: Int,
    onBackPressed: () -> Unit,
    viewModel: ExerciseViewModel = viewModel()
) {
    val exercise = viewModel.getExerciseById(exerciseId).collectAsState(initial = null)
    val logs = viewModel.getLogsForExercise(exerciseId).collectAsState(initial = emptyList())
    val context = LocalContext.current
    val preferences = remember { Preferences(context) }
    val viewWindow by preferences.viewWindowFlow.collectAsState(initial = 0)
    val weightedRegression by viewModel.weightedRegressionFlow.collectAsState(initial = false)
    val regressionWindow by viewModel.regressionWindowFlow.collectAsState(initial = 0)

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedLog by remember { mutableStateOf<ExerciseLog?>(null) }

    val fitToLastSession by preferences.fitToLastSessionFlow.collectAsState(initial = true)

    val regression by remember(logs.value, weightedRegression, regressionWindow, fitToLastSession) {
        derivedStateOf {
            if (logs.value.size >= 2) {
                viewModel.calculateLinearRegression(logs.value, weightedRegression, regressionWindow, fitToLastSession)
            } else {
                null
            }
        }
    }

    // Calculate prefilled values
    val lastLog = logs.value.maxByOrNull { it.date }
    var reps by remember(lastLog) { mutableStateOf(lastLog?.reps?.toString() ?: "") }

    val predictedOneRM = remember(logs.value, regression, fitToLastSession) {
        regression?.let { (slope, intercept, adjustment) ->
            val x = logs.value.size.toDouble()
            slope * x + intercept
        }
    }

    val calculatedWeight = remember(predictedOneRM, reps) {
        predictedOneRM?.let { oneRM ->
            val targetReps = reps.toIntOrNull() ?: lastLog?.reps ?: 0
            adjustWeightForReps(oneRM.toFloat(), targetReps)
        }
    }

    var weight by remember(calculatedWeight, exercise.value, lastLog) {
        mutableStateOf(
            calculatedWeight?.let { w ->
                val weightStep = exercise.value?.weightSteps ?: 1.0
                if (w.isFinite() && weightStep > 0) {
                    roundToNearestWeightStep(w, weightStep).toString()
                } else {
                    lastLog?.weight?.toString() ?: ""
                }
            } ?: lastLog?.weight?.toString() ?: ""
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise.value?.name ?: "Exercise Details") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
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
                                weightStep = ex.weightSteps,
                                onIncrement = {
                                    weight = (weight.toFloatOrNull()?.plus(ex.weightSteps.toFloat()) ?: ex.weightSteps.toFloat()).toString()
                                },
                                onDecrement = {
                                    weight = (weight.toFloatOrNull()?.minus(ex.weightSteps.toFloat())?.coerceAtLeast(0f) ?: 0f).toString()
                                }
                            )
                            RepsInputWithButtons(
                                reps = reps,
                                onRepsChange = { newReps ->
                                    reps = newReps
                                    calculatedWeight?.let { w ->
                                        val weightStep = ex.weightSteps
                                        weight = roundToNearestWeightStep(w, weightStep).toString()
                                    }
                                },
                                onIncrement = {
                                    reps = (reps.toIntOrNull()?.plus(1) ?: 1).toString()
                                    calculatedWeight?.let { w ->
                                        val weightStep = ex.weightSteps
                                        weight = roundToNearestWeightStep(w, weightStep).toString()
                                    }
                                },
                                onDecrement = {
                                    reps = (reps.toIntOrNull()?.minus(1)?.coerceAtLeast(0) ?: 0).toString()
                                    calculatedWeight?.let { w ->
                                        val weightStep = ex.weightSteps
                                        weight = roundToNearestWeightStep(w, weightStep).toString()
                                    }
                                }
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
                                    // Recalculate weight based on new log
                                    calculatedWeight?.let { w ->
                                        val weightStep = ex.weightSteps
                                        weight = roundToNearestWeightStep(w, weightStep).toString()
                                    }
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
                        WeightProgressChart(
                            logs = logs.value.map { Quadruple(it.exerciseId, it.weight, it.reps, it.date) },
                            viewWindow = viewWindow,
                            regression = regression,
                            fitToLastSession = fitToLastSession
                        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightInputWithButtons(
    weight: String,
    onWeightChange: (String) -> Unit,
    weightStep: Double,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalIconButton(
            onClick = onDecrement,
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
            onClick = onIncrement,
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
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalIconButton(
            onClick = onDecrement,
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
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
        )
        Spacer(modifier = Modifier.width(8.dp))
        FilledTonalIconButton(
            onClick = onIncrement,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase Reps")
        }
    }
}


@Composable
fun ExerciseLogItem(log: ExerciseLog, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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


@OptIn(ExperimentalMaterial3Api::class)
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
    var date by remember { mutableStateOf(log.date) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date.time)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Exercise Log") },
        text = {
            Column {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Reps") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dateFormatter.format(date),
                    onValueChange = { },
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select date"
                            )
                        }
                    },
                    readOnly = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedLog = log.copy(
                        weight = weight.toFloatOrNull() ?: log.weight,
                        reps = reps.toIntOrNull() ?: log.reps,
                        date = date
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

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            date = Date(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }
}