package com.acetylsalicylsaeure.platten.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.acetylsalicylsaeure.platten.data.Preferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun ExercisesSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val themePreferences = remember { Preferences(context) }
    val scope = rememberCoroutineScope()

    val fitToLastSession by themePreferences.fitToLastSessionFlow.collectAsState(initial = false)
    val weightedRegression by themePreferences.weightedRegressionFlow.collectAsState(initial = false)
    val regressionWindow by themePreferences.regressionWindowFlow.collectAsState(initial = 0)
    val viewWindow by themePreferences.viewWindowFlow.collectAsState(initial = 0)

    var regressionWindowInput by remember { mutableStateOf(regressionWindow.toString()) }
    var viewWindowInput by remember { mutableStateOf(viewWindow.toString()) }

    LaunchedEffect(regressionWindow) {
        regressionWindowInput = regressionWindow.toString()
    }

    LaunchedEffect(viewWindow) {
        viewWindowInput = viewWindow.toString()
    }

    // Debounced update for regression window
    LaunchedEffect(regressionWindowInput) {
        snapshotFlow { regressionWindowInput }
            .debounce(300)
            .collect { input ->
                input.toIntOrNull()?.let { themePreferences.setRegressionWindow(it) }
            }
    }

    // Debounced update for view window
    LaunchedEffect(viewWindowInput) {
        snapshotFlow { viewWindowInput }
            .debounce(300)
            .collect { input ->
                input.toIntOrNull()?.let { themePreferences.setViewWindow(it) }
            }
    }

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
                .fillMaxWidth()
        ) {
            SettingsSwitch(
                title = "Fit to last session",
                checked = fitToLastSession,
                onCheckedChange = {
                    scope.launch {
                        themePreferences.setFitToLastSession(it)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSwitch(
                title = "Weighted regression",
                checked = weightedRegression,
                onCheckedChange = {
                    scope.launch {
                        themePreferences.setWeightedRegression(it)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsNumberInput(
                title = "Regression window",
                value = regressionWindowInput,
                onValueChange = { regressionWindowInput = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsNumberInput(
                title = "View window",
                value = viewWindowInput,
                onValueChange = { viewWindowInput = it }
            )
        }
    }
}

@Composable
fun SettingsNumberInput(
    title: String,
    value: String,
    onValueChange: (String) -> Unit
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
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(100.dp)
        )
    }
}