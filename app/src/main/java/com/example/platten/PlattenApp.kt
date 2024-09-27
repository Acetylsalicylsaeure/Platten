package com.example.platten

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.platten.data.ThemePreferences
import com.example.platten.ui.screens.MainScreen
import com.example.platten.ui.screens.SettingsScreen
import com.example.platten.ui.screens.ColorSettingsScreen
import com.example.platten.ui.screens.ExerciseDetailScreen
import com.example.platten.ui.screens.ExercisesSettingsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PlattenApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Initialize theme preferences
    LaunchedEffect(Unit) {
        val themePreferences = ThemePreferences(context)
        CoroutineScope(Dispatchers.Default).launch {
            themePreferences.initializeThemePreference()
        }
    }

    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("settings/colors") { ColorSettingsScreen(navController) }
        composable("settings/exercises") { ExercisesSettingsScreen(navController) }
        composable("exercise/{exerciseId}") { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId")?.toIntOrNull() ?: return@composable
            ExerciseDetailScreen(navController, exerciseId)
        }
    }
}