package com.acetylsalicylsaeure.platten.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acetylsalicylsaeure.platten.viewmodel.ExerciseViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.acetylsalicylsaeure.platten.viewmodel.WorkoutViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: ExerciseViewModel = viewModel()) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { selectedTabIndex = 0 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTabIndex == 0)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surface,
                                contentColor = if (selectedTabIndex == 0)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Exercises")
                        }
                        Button(
                            onClick = { selectedTabIndex = 1 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTabIndex == 1)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surface,
                                contentColor = if (selectedTabIndex == 1)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("Workouts")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTabIndex) {
                0 -> ExercisesTab(navController, viewModel)
                1 -> WorkoutsTab(navController)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExercisesTab(navController: NavController, viewModel: ExerciseViewModel) {
    ExercisesScreen(navController, viewModel)
}

@Composable
fun WorkoutsTab(navController: NavController, viewModel: WorkoutViewModel = viewModel()) {
    WorkoutScreen(navController, viewModel)
}