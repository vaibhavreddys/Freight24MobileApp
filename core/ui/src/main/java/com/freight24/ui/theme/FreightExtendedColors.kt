package com.freight24.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class FreightExtendedColors(
    val successColor: Color,
    val warningColor: Color,
    val infoColor: Color,
    val settingsColor: Color,
    val onSuccessColor: Color,
    val dialogScrimColor: Color,
    val surfaceGray: Color,
    val borderOverlay: Color,
    val surfaceSuccessLight: Color,
    val surfaceSuccessLighter: Color,
)

internal val LocalFreightExtendedColors = staticCompositionLocalOf {
    FreightExtendedColors(
        successColor = Color.Unspecified,
        warningColor = Color.Unspecified,
        infoColor = Color.Unspecified,
        settingsColor = Color.Unspecified,
        onSuccessColor = Color.Unspecified,
        dialogScrimColor = Color.Unspecified,
        surfaceGray = Color(0xFFF3F3F3),
        borderOverlay = Color(0xFFF1F5F9),
        surfaceSuccessLight = Color(0xFFECFDF5),
        surfaceSuccessLighter = Color(0xFFF0FDF4),
    )
}

object FreightThemeTokens {
    val colors: FreightExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFreightExtendedColors.current
}
