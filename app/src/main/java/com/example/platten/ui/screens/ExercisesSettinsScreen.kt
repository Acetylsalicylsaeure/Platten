package com.example.platten.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesSettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercise Settings") },
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
                .fillMaxSize()
        ) {
            Text(
                "Exercise settings and configurations will be added here in future updates.",
                style = MaterialTheme.typography.bodyLarge
            )

            // Placeholder for future settings
            // Example:
            // Switch(
            //     checked = false,
            //     onCheckedChange = { /* TODO: Implement functionality */ },
            //     modifier = Modifier.padding(top = 16.dp)
            // ) {
            //     Text("Enable advanced exercise tracking")
            // }
        }
    }
}