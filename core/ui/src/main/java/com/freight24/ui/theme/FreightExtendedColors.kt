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
)

internal val LocalFreightExtendedColors = staticCompositionLocalOf {
    FreightExtendedColors(
        successColor = Color.Unspecified,
        warningColor = Color.Unspecified,
        infoColor = Color.Unspecified,
        settingsColor = Color.Unspecified,
        onSuccessColor = Color.Unspecified,
        dialogScrimColor = Color.Unspecified,
    )
}

object FreightThemeTokens {
    val colors: FreightExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFreightExtendedColors.current
}
