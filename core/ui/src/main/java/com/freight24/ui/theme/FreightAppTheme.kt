package com.freight24.ui.theme

/**
 * Available app color schemes.
 *
 * Each value maps to a complete M3 [ColorScheme] + [FreightExtendedColors]
 * set defined in [Theme.kt]. The selected value is persisted via
 * [com.freight24.common.manager.ThemePreferenceManager] and applied in
 * [Freight24Theme].
 */
enum class FreightAppTheme(
    /** Display label shown in the settings screen. */
    val label: String,
) {
    INDIGO("Indigo"),
    GREEN("Green"),
}
