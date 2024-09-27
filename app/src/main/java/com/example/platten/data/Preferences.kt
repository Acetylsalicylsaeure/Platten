package com.example.platten.data

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class Preferences(private val context: Context) {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
    private val FIRST_TIME_KEY = booleanPreferencesKey("first_time")

    private val FIT_TO_LAST_SESSION_KEY = booleanPreferencesKey("fit_to_last_session")
    private val WEIGHTED_REGRESSION_KEY = booleanPreferencesKey("weighted_regression")
    private val REGRESSION_WINDOW_KEY = intPreferencesKey("regression_window")
    private val VIEW_WINDOW_KEY = intPreferencesKey("view_window")

    val darkModeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    val dynamicColorFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DYNAMIC_COLOR_KEY] ?: true
        }

    private fun isSystemInDarkTheme(): Boolean {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

    suspend fun initializeThemePreference() {
        context.dataStore.edit { preferences ->
            val isFirstTime = preferences[FIRST_TIME_KEY] ?: true
            if (isFirstTime) {
                preferences[DARK_MODE_KEY] = isSystemInDarkTheme()
                preferences[FIRST_TIME_KEY] = false
            }
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = enabled
        }
    }

    // Exercise preferences
    val fitToLastSessionFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[FIT_TO_LAST_SESSION_KEY] ?: false }

    val weightedRegressionFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[WEIGHTED_REGRESSION_KEY] ?: false }

    val regressionWindowFlow: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[REGRESSION_WINDOW_KEY] ?: 0 }

    val viewWindowFlow: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[VIEW_WINDOW_KEY] ?: 0 }


    // functions for exercise settings
    suspend fun setFitToLastSession(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FIT_TO_LAST_SESSION_KEY] = enabled
        }
    }

    suspend fun setWeightedRegression(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WEIGHTED_REGRESSION_KEY] = enabled
        }
    }

    suspend fun setRegressionWindow(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[REGRESSION_WINDOW_KEY] = value
        }
    }

    suspend fun setViewWindow(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[VIEW_WINDOW_KEY] = value
        }
    }
}