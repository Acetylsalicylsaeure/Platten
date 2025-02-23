package com.acetylsalicylsaeure.platten.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.navigation.NavController
import com.acetylsalicylsaeure.platten.viewmodel.ExerciseViewModel
import com.acetylsalicylsaeure.platten.data.Exercise
import com.acetylsalicylsaeure.platten.ui.components.AddExerciseDialog
import com.acetylsalicylsaeure.platten.ui.components.EditExerciseDialog
import com.acetylsalicylsaeure.platten.ui.components.ExerciseItem

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExercisesScreen(navController: NavController, viewModel: ExerciseViewModel) {
    val sortedExercisesWithLastTrained by viewModel.sortedExercisesWithLastTrained.collectAsState()
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
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

        FloatingActionButton(
            onClick = { showAddExerciseDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Exercise")
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
            },
            onHide = { id ->
                viewModel.setExerciseHidden(id, true)
                showEditDialog = false
            }
        )
    }
}