package com.acetylsalicylsaeure.platten.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.acetylsalicylsaeure.platten.data.Exercise
import com.acetylsalicylsaeure.platten.viewmodel.ExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHiddenExercisesScreen(
    navController: NavController,
    viewModel: ExerciseViewModel = viewModel()
) {
    val allExercises by viewModel.allExercises.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hidden Exercises") },
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
                .padding(16.dp)
        ) {
            items(allExercises.filter { exercise -> exercise.hidden }) { exercise ->
                HiddenExerciseItem(
                    exercise = exercise,
                    onUnhide = { viewModel.setExerciseHidden(exercise.id, false) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun HiddenExerciseItem(
    exercise: Exercise,
    onUnhide: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onUnhide) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = "Unhide exercise",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}