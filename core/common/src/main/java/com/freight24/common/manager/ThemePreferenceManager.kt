package com.freight24.common.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_theme_prefs")

/**
 * Persists the user's chosen app theme (color scheme) and dark mode setting.
 *
 * This manager follows the same DataStore pattern as [com.freight24.auth.AuthDataStore]
 * and is accessible from both app-trucker and app-connect via Hilt injection.
 */
@Singleton
class ThemePreferenceManager
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        companion object {
            private val THEME_KEY = intPreferencesKey("app_theme")
            private val DARK_MODE_KEY = stringPreferencesKey("dark_mode")

            /** Theme constants — must match [ThemeChoice] ordinals. */
            const val THEME_INDIGO = 0
            const val THEME_GREEN = 1

            const val DARK_MODE_FOLLOW_SYSTEM = "system"
            const val DARK_MODE_ON = "on"
            const val DARK_MODE_OFF = "off"
        }

        /** The selected app theme (color scheme). Defaults to Indigo (0). */
        val themeChoice: Flow<Int> =
            context.themeDataStore.data.map { prefs ->
                prefs[THEME_KEY] ?: THEME_INDIGO
            }

        /** Dark-mode override: "system" | "on" | "off". Defaults to "system". */
        val darkModeOverride: Flow<String> =
            context.themeDataStore.data.map { prefs ->
                prefs[DARK_MODE_KEY] ?: DARK_MODE_FOLLOW_SYSTEM
            }

        suspend fun setThemeChoice(theme: Int) {
            context.themeDataStore.edit { prefs ->
                prefs[THEME_KEY] = theme
            }
        }

        suspend fun setDarkModeOverride(mode: String) {
            context.themeDataStore.edit { prefs ->
                prefs[DARK_MODE_KEY] = mode
            }
        }
    }
