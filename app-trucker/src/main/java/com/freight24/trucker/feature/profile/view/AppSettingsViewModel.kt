package com.freight24.trucker.feature.profile.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.manager.ThemePreferenceManager
import com.freight24.ui.theme.FreightAppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingsViewModel
    @Inject
    constructor(
        private val themePreferenceManager: ThemePreferenceManager,
    ) : ViewModel() {
        val themeChoice =
            themePreferenceManager.themeChoice
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemePreferenceManager.THEME_LOGISTICS)

        val darkModeOverride =
            themePreferenceManager.darkModeOverride
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemePreferenceManager.DARK_MODE_FOLLOW_SYSTEM)

        fun setTheme(theme: FreightAppTheme) {
            viewModelScope.launch {
                themePreferenceManager.setThemeChoice(theme.ordinal)
            }
        }

        fun setDarkMode(mode: String) {
            viewModelScope.launch {
                themePreferenceManager.setDarkModeOverride(mode)
            }
        }
    }
