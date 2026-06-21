package com.freight24.ui.theme

/**
 * Available app color schemes.
 *
 * Each value maps to a complete M3 [androidx.compose.material3.ColorScheme] +
 * [FreightExtendedColors] set defined in [Theme.kt]. The selected value is
 * persisted via
 * [com.freight24.common.manager.ThemePreferenceManager] and applied in
 * [Freight24Theme].
 */
enum class FreightAppTheme(
    /** Display label shown in the settings screen. */
    val label: String,
    /** Short subtitle describing the theme; rendered beneath the label. */
    val subtitle: String,
) {
    INDIGO("Indigo", "Deep purple accent"),
    GREEN("Green", "Classic Freight24 green"),
    LOGISTICS("Logistics", "Forest green + brick red"),
}
