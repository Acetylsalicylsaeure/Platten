package com.acetylsalicylsaeure.platten.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.acetylsalicylsaeure.platten.data.Preferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val preferences = remember { Preferences(context) }
    val scope = rememberCoroutineScope()
    val darkMode by preferences.darkModeFlow.collectAsState(initial = false)
    val dynamicColor by preferences.dynamicColorFlow.collectAsState(initial = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Color Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            SettingsSwitch(
                title = "Dark Mode",
                checked = darkMode,
                onCheckedChange = {
                    scope.launch {
                        preferences.setDarkMode(it)
                    }
                }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSwitch(
                    title = "Dynamic Colors",
                    checked = dynamicColor,
                    onCheckedChange = {
                        scope.launch {
                            preferences.setDynamicColor(it)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}