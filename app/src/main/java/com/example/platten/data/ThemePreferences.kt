package com.example.platten.data

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemePreferences(private val context: Context) {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
    private val FIRST_TIME_KEY = booleanPreferencesKey("first_time")

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
}