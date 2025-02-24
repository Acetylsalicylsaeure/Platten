package com.acetylsalicylsaeure.platten.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * A simple navigation state manager to keep track of which tab was active
 * before navigating to detail screens.
 */
@Composable
fun rememberNavigationState(): NavigationState {
    var activeTab by rememberSaveable { mutableStateOf(0) }
    var previousScreen by rememberSaveable { mutableStateOf("") }

    return NavigationState(
        activeTab = activeTab,
        onTabChanged = { newTab -> activeTab = newTab },
        previousScreen = previousScreen,
        onScreenChanged = { screen -> previousScreen = screen }
    )
}

data class NavigationState(
    val activeTab: Int,
    val onTabChanged: (Int) -> Unit,
    val previousScreen: String,
    val onScreenChanged: (String) -> Unit
)