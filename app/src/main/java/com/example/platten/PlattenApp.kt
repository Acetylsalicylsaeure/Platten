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
    }
}