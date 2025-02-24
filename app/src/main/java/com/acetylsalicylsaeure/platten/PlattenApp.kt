package com.acetylsalicylsaeure.platten

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.acetylsalicylsaeure.platten.data.Preferences
import com.acetylsalicylsaeure.platten.navigation.NavigationState
import com.acetylsalicylsaeure.platten.navigation.rememberNavigationState
import com.acetylsalicylsaeure.platten.ui.screens.BackupRestoreScreen
import com.acetylsalicylsaeure.platten.ui.screens.ColorSettingsScreen
import com.acetylsalicylsaeure.platten.ui.screens.ExerciseDetailScreen
import com.acetylsalicylsaeure.platten.ui.screens.ExercisesSettingsScreen
import com.acetylsalicylsaeure.platten.ui.screens.MainScreen
import com.acetylsalicylsaeure.platten.ui.screens.ManageHiddenExercisesScreen
import com.acetylsalicylsaeure.platten.ui.screens.SettingsScreen
import com.acetylsalicylsaeure.platten.ui.screens.WorkoutDetailScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlattenApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val navigationState = rememberNavigationState()

    // Initialize theme preferences
    LaunchedEffect(Unit) {
        val preferences = Preferences(context)
        CoroutineScope(Dispatchers.Default).launch {
            preferences.initializePreference()
        }
    }

    NavHost(navController = navController, startDestination = "main") {
        // Main screen with tabs
        composable("main") {
            MainScreen(
                navController = navController,
                navigationState = navigationState
            )
        }

        composable("settings") {
            BackHandler {
                navController.navigate("main") {
                    popUpTo("main") { inclusive = true }
                }
            }
            SettingsScreen(navController)
        }

        composable("settings/colors") { ColorSettingsScreen(navController) }
        composable("settings/exercises") { ExercisesSettingsScreen(navController) }
        composable("settings/backup-restore") { BackupRestoreScreen(navController) }

        composable("exercise/{exerciseId}") { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId")?.toIntOrNull() ?: return@composable

            // Track where we came from for proper back navigation
            val comeFromWorkout = navigationState.previousScreen.startsWith("workout/")

            BackHandler {
                if (comeFromWorkout) {
                    navController.popBackStack()
                } else {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            }

            ExerciseDetailScreen(
                navController = navController,
                exerciseId = exerciseId,
                onBackPressed = {
                    if (comeFromWorkout) {
                        navController.popBackStack()
                    } else {
                        navController.navigate("main") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("settings/hidden-exercises") { ManageHiddenExercisesScreen(navController) }

        composable("workout/{workoutId}") { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getString("workoutId")?.toIntOrNull() ?: return@composable

            // Save the current screen for back navigation context
            LaunchedEffect(Unit) {
                navigationState.onScreenChanged("workout/${workoutId}")
            }

            BackHandler {
                // Return to main screen and make sure the workouts tab is active
                navigationState.onTabChanged(1) // Set workouts tab as active
                navController.navigate("main") {
                    popUpTo("main") { inclusive = true }
                }
            }

            WorkoutDetailScreen(
                navController = navController,
                workoutId = workoutId,
                navigationState = navigationState,
                onBackPressed = {
                    navigationState.onTabChanged(1) // Set workouts tab as active
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}