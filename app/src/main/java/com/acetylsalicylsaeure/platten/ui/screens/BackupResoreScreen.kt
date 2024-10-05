package com.acetylsalicylsaeure.platten.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.acetylsalicylsaeure.platten.data.DatabaseBackupManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val databaseBackupManager = remember { DatabaseBackupManager(context) }

    var backupMessage by remember { mutableStateOf<String?>(null) }
    var restoreMessage by remember { mutableStateOf<String?>(null) }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    databaseBackupManager.backupDatabase(uri)
                    backupMessage = "Backup completed successfully"
                } catch (e: Exception) {
                    backupMessage = "Backup failed: ${e.message}"
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    databaseBackupManager.restoreDatabase(uri)
                    restoreMessage = "Restore completed successfully"
                } catch (e: Exception) {
                    restoreMessage = "Restore failed: ${e.message}"
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup and Restore") },
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
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { backupLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Backup Database")
            }

            Button(
                onClick = { restoreLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restore Database")
            }

            backupMessage?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }

            restoreMessage?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
