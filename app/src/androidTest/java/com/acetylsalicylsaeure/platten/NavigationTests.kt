package com.acetylsalicylsaeure.platten.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Simple unit test for NavigationState without any Compose or Android dependencies.
 *
 * This test directly tests the behavior of the NavigationState data class
 * with explicit tracking of state changes.
 */
class SimpleNavigationTest {

    @Test
    fun testNavigationManagement() {
        // Manually track state for verification
        var currentTab = 0
        var currentScreen = ""

        // Create NavigationState with callback handlers
        val navigationState = NavigationState(
            activeTab = currentTab,
            onTabChanged = { newTab ->
                currentTab = newTab
            },
            previousScreen = currentScreen,
            onScreenChanged = { newScreen ->
                currentScreen = newScreen
            }
        )

        // Test 1: Initial state
        assertEquals(0, navigationState.activeTab)
        assertEquals("", navigationState.previousScreen)

        // Test 2: Change tab
        navigationState.onTabChanged(1)
        assertEquals(1, currentTab)

        // Test 3: Change screen
        navigationState.onScreenChanged("workout/1")
        assertEquals("workout/1", currentScreen)

        // Test 4: Simulate workout -> exercise -> back flow

        // Create a new state with the current values
        val updatedNav = NavigationState(
            activeTab = currentTab,
            onTabChanged = { newTab -> currentTab = newTab },
            previousScreen = currentScreen,
            onScreenChanged = { newScreen -> currentScreen = newScreen }
        )

        // Now we should have:
        // - Active tab = 1 (Workouts)
        // - Previous screen = "workout/1"
        assertEquals(1, updatedNav.activeTab)
        assertEquals("workout/1", updatedNav.previousScreen)

        // Navigate to exercise
        updatedNav.onScreenChanged("exercise/2")
        assertEquals("exercise/2", currentScreen)

        // Go back to workout
        updatedNav.onScreenChanged("workout/1")
        assertEquals("workout/1", currentScreen)

        // Go back to main
        updatedNav.onScreenChanged("main")
        assertEquals("main", currentScreen)

        // Verify tab is still on Workouts
        assertEquals(1, currentTab)
    }

    @Test
    fun testNavigationStateIsImmutable() {
        // Verify that NavigationState is immutable and state changes
        // are propagated through callbacks rather than modifying the original instance

        var tabState = 0
        var screenState = ""

        val navState = NavigationState(
            activeTab = tabState,
            onTabChanged = { tabState = it },
            previousScreen = screenState,
            onScreenChanged = { screenState = it }
        )

        // Initial state
        assertEquals(0, navState.activeTab)
        assertEquals("", navState.previousScreen)

        // Change state through callbacks
        navState.onTabChanged(1)
        navState.onScreenChanged("workout/1")

        // Original NavigationState object is unchanged
        // But our tracking variables reflect the changes
        assertEquals(0, navState.activeTab)
        assertEquals("", navState.previousScreen)
        assertEquals(1, tabState)
        assertEquals("workout/1", screenState)

        // Create a new state with updated values
        val updatedNavState = NavigationState(
            activeTab = tabState,
            onTabChanged = { tabState = it },
            previousScreen = screenState,
            onScreenChanged = { screenState = it }
        )

        // New state reflects current values
        assertEquals(1, updatedNavState.activeTab)
        assertEquals("workout/1", updatedNavState.previousScreen)
    }
}