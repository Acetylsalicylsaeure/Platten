package com.acetylsalicylsaeure.platten

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.acetylsalicylsaeure.platten.data.Preferences
import com.acetylsalicylsaeure.platten.ui.screens.BackupRestoreScreen
import com.acetylsalicylsaeure.platten.ui.screens.MainScreen
import com.acetylsalicylsaeure.platten.ui.screens.SettingsScreen
import com.acetylsalicylsaeure.platten.ui.screens.ColorSettingsScreen
import com.acetylsalicylsaeure.platten.ui.screens.ExerciseDetailScreen
import com.acetylsalicylsaeure.platten.ui.screens.ExercisesSettingsScreen
import com.acetylsalicylsaeure.platten.ui.screens.ManageHiddenExercisesScreen
import com.acetylsalicylsaeure.platten.ui.screens.WorkoutDetailScreen
import com.acetylsalicylsaeure.platten.ui.screens.WorkoutScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlattenApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Initialize theme preferences
    LaunchedEffect(Unit) {
        val preferences = Preferences(context)
        CoroutineScope(Dispatchers.Default).launch {
            preferences.initializePreference()
        }
    }

    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("settings/colors") { ColorSettingsScreen(navController) }
        composable("settings/exercises") { ExercisesSettingsScreen(navController) }
        composable("settings/backup-restore") { BackupRestoreScreen(navController) }
        composable("exercise/{exerciseId}") { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId")?.toIntOrNull() ?: return@composable
            ExerciseDetailScreen(navController, exerciseId)
        }
        composable("settings/hidden-exercises") { ManageHiddenExercisesScreen(navController) }
        composable("settings/hidden-exercises") { ManageHiddenExercisesScreen(navController) }
        composable("workout/{workoutId}") { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getString("workoutId")?.toIntOrNull() ?: return@composable
            WorkoutDetailScreen(navController, workoutId)
        }
        composable("workouts") {
            WorkoutScreen(navController)
        }
    }
}