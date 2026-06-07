package com.freight24.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freight24.ui.theme.FreightAppTheme
import com.freight24.ui.theme.FreightThemeTokens

/**
 * Reusable App Settings screen — shared between app-trucker and app-connect.
 *
 * This composable is **stateless**: it receives the current selections and
 * callbacks from the caller, so each module can wire its own DataStore or
 * ViewModel without depending on a shared ViewModel class.
 *
 * @param currentTheme The currently-selected [FreightAppTheme].
 * @param currentDarkMode One of `"system"`, `"on"`, or `"off"`.
 * @param onThemeChange Called when the user picks a new [FreightAppTheme].
 * @param onDarkModeChange Called when the user picks a new dark-mode setting.
 * @param onBackClick Called when the user taps the back arrow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    currentTheme: FreightAppTheme,
    currentDarkMode: String,
    onThemeChange: (FreightAppTheme) -> Unit,
    onDarkModeChange: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = {
                    Text(
                        text = "App Settings",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // ---- App Theme Section ----
            SettingsSectionHeader(
                icon = Icons.Default.Palette,
                title = "App Theme",
            )

            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            ) {
                FreightAppTheme.entries.forEachIndexed { index, theme ->
                    SettingsRadioButtonRow(
                        label = theme.label,
                        subtitle = when (theme) {
                            FreightAppTheme.INDIGO -> "Deep purple accent"
                            FreightAppTheme.GREEN -> "Classic Freight24 green"
                        },
                        selected = currentTheme == theme,
                        onClick = { onThemeChange(theme) },
                        accentColor = when (theme) {
                            FreightAppTheme.INDIGO -> MaterialTheme.colorScheme.primary
                            FreightAppTheme.GREEN -> Color(0xFF509367)
                        },
                    )
                    if (index < FreightAppTheme.entries.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---- Dark Mode Section ----
            SettingsSectionHeader(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
            )

            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            ) {
                val darkModeOptions =
                    listOf(
                        "system" to "Follow System",
                        "on" to "Always On",
                        "off" to "Always Off",
                    )
                darkModeOptions.forEachIndexed { index, (value, label) ->
                    SettingsRadioButtonRow(
                        label = label,
                        subtitle =
                            when (value) {
                                "system" -> "Match your device setting"
                                "on" -> "Use dark colors always"
                                "off" -> "Use light colors always"
                                else -> null
                            },
                        selected = currentDarkMode == value,
                        onClick = { onDarkModeChange(value) },
                    )
                    if (index < darkModeOptions.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Internal helpers
// ---------------------------------------------------------------------------

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FreightThemeTokens.colors.settings,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SettingsRadioButtonRow(
    label: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null, // Row clickable handles it
            colors =
                RadioButtonDefaults.colors(
                    selectedColor = accentColor,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
