package com.acetylsalicylsaeure.platten.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .fillMaxSize()
        ) {
            SettingsItem(
                title = "Colors",
                onClick = { navController.navigate("settings/colors") }
            )
            SettingsItem(
                title = "Exercises",
                onClick = { navController.navigate("settings/exercises") }
            )
            SettingsItem(
                title = "Backup and Restore",
                onClick = { navController.navigate("settings/backup-restore") }
            )
            // Add more settings items here as needed
            SettingsItem(
                title = "Hidden Exercises",
                onClick = { navController.navigate("settings/hidden-exercises") }
            )
        }
    }
}

@Composable
fun SettingsItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title)
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate to $title"
        )
    }
}