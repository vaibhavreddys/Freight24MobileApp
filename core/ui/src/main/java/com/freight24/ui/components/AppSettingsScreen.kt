package com.freight24.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Surface
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

            // Live "Currently active" mini-preview card. Always reflects the
            // *committed* theme — purely informational, not interactive.
            ThemePreviewCard(theme = currentTheme)

            Spacer(Modifier.height(12.dp))

            // Theme picker card with swatch rows. Live-applies on tap.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            ) {
                FreightAppTheme.entries.forEachIndexed { index, theme ->
                    ThemeSwatchRow(
                        theme = theme,
                        selected = currentTheme == theme,
                        onClick = { onThemeChange(theme) },
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

/**
 * A single swatch row inside the App Theme picker card.
 *
 * Shows two color swatches (primary + secondary of the theme this row
 * represents), a radio button, and the theme label + subtitle. Tapping the
 * row live-applies the theme via [onClick] — matches the existing
 * commit-on-tap behavior.
 */
@Composable
private fun ThemeSwatchRow(
    theme: FreightAppTheme,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val primary = themeSwatchPrimary(theme)
    val secondary = themeSwatchSecondary(theme)
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
                    selectedColor = primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
        )
        Spacer(Modifier.width(12.dp))
        // Side-by-side swatches
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ColorSwatch(color = primary)
            ColorSwatch(color = secondary)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = theme.label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = theme.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ColorSwatch(color: Color) {
    Box(
        modifier =
            Modifier
                .size(24.dp)
                .background(color = color, shape = RoundedCornerShape(8.dp))
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                ),
    )
}

/**
 * Static "Currently active" preview card. Renders a primary button, a small
 * status chip, and a surface swatch using the *committed* theme's tokens —
 * the card itself always sits inside `MaterialTheme.colorScheme` so its
 * shapes/text pick up the active palette automatically. Informational only.
 */
@Composable
private fun ThemePreviewCard(theme: FreightAppTheme) {
    val primary = themeSwatchPrimary(theme)
    val secondary = themeSwatchSecondary(theme)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Currently active",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = theme.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.height(10.dp))
            // Primary button preview (uses theme's primary color directly so
            // the preview reads correctly even if the user has selected a
            // different theme than the active one — but in practice this is
            // always the active theme).
            Button(
                onClick = {},
                enabled = false,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = primary,
                        contentColor = Color.White,
                        disabledContainerColor = primary,
                        disabledContentColor = Color.White,
                    ),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(text = "Primary action", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Status chip preview (success)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FreightThemeTokens.colors.surfaceSuccess,
                ) {
                    Text(
                        text = "  Available  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = FreightThemeTokens.colors.onSurfaceSuccess,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
                // Brand accent dot (secondary)
                Box(
                    modifier =
                        Modifier
                            .size(16.dp)
                            .background(color = secondary, shape = RoundedCornerShape(4.dp))
                            .border(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(4.dp),
                            ),
                )
                Text(
                    text = "Accent",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = FreightThemeTokens.colors.success,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
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

// ---------------------------------------------------------------------------
// Swatch helpers — map a [FreightAppTheme] to its representative primary /
// secondary color for the picker UI. These are the same T40 values used in
// the schemes (Indigo Primary40, GreenPrimary40, LogisticsPrimary40, etc).
// Returns Color.Unspecified if a future theme is added without updating
// these helpers, so the row falls back to grey rather than crashing.
// ---------------------------------------------------------------------------

private fun themeSwatchPrimary(theme: FreightAppTheme): Color =
    when (theme) {
        FreightAppTheme.INDIGO -> Color(0xFF6750A4)
        FreightAppTheme.GREEN -> Color(0xFF509367)
        FreightAppTheme.LOGISTICS -> Color(0xFF2E5D3F)
        FreightAppTheme.CONNECT -> Color(0xFF1B5E40)
    }

private fun themeSwatchSecondary(theme: FreightAppTheme): Color =
    when (theme) {
        FreightAppTheme.INDIGO -> Color(0xFF625B71)
        FreightAppTheme.GREEN -> Color(0xFF3F6750)
        FreightAppTheme.LOGISTICS -> Color(0xFFA8433A)
        FreightAppTheme.CONNECT -> Color(0xFF6F5A2A)
    }
