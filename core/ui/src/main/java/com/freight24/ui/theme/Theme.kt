@file:Suppress("DEPRECATION")

package com.freight24.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme =
    lightColorScheme(
        primary = PrimaryGreen,
        onPrimary = OnPrimary,
        primaryContainer = PrimaryContainer,
        onPrimaryContainer = OnPrimaryContainer,
        inversePrimary = InversePrimary,
        secondary = SecondarySage,
        onSecondary = OnSecondary,
        secondaryContainer = SecondaryContainer,
        onSecondaryContainer = OnSecondaryContainer,
        tertiary = TertiarySand,
        onTertiary = OnTertiary,
        tertiaryContainer = TertiaryContainer,
        onTertiaryContainer = OnTertiaryContainer,
        error = ErrorRed,
        onError = OnError,
        errorContainer = ErrorContainer,
        onErrorContainer = OnErrorContainer,
        background = BackgroundLight,
        onBackground = OnSurfaceLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
        surfaceVariant = SurfaceContainerLight,
        onSurfaceVariant = OnSurfaceVariantLight,
        surfaceDim = SurfaceDimLight,
        surfaceBright = SurfaceBrightLight,
        surfaceContainerLowest = SurfaceContainerLowestLight,
        surfaceContainerLow = SurfaceContainerLowLight,
        surfaceContainer = SurfaceContainerLight,
        surfaceContainerHigh = SurfaceContainerHighLight,
        surfaceContainerHighest = SurfaceContainerHighestLight,
        outline = OutlineLight,
        outlineVariant = OutlineVariantLight,
        inverseSurface = InverseSurfaceLight,
        inverseOnSurface = InverseOnSurfaceLight,
        surfaceTint = PrimaryGreenGradient,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = PrimaryGreen,
        onPrimary = OnPrimaryInverse,
        primaryContainer = PrimaryContainer,
        onPrimaryContainer = OnPrimaryContainer,
        inversePrimary = PrimaryGreen,
        secondary = SecondaryContainer,
        onSecondary = OnSecondaryContainer,
        secondaryContainer = SecondarySage,
        onSecondaryContainer = OnSecondary,
        tertiary = OnTertiaryContainer,
        onTertiary = OnTertiary,
        tertiaryContainer = TertiaryContainer,
        onTertiaryContainer = OnTertiaryContainer,
        error = ErrorRed,
        onError = OnError,
        errorContainer = ErrorContainer,
        onErrorContainer = OnErrorContainer,
        background = BackgroundDark,
        onBackground = OnSurfaceDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        surfaceVariant = SurfaceContainerDark,
        onSurfaceVariant = OnSurfaceVariantDark,
        surfaceDim = SurfaceDimDark,
        surfaceBright = SurfaceBrightDark,
        surfaceContainerLowest = SurfaceContainerLowestDark,
        surfaceContainerLow = SurfaceContainerLowDark,
        surfaceContainer = SurfaceContainerDark,
        surfaceContainerHigh = SurfaceContainerHighDark,
        surfaceContainerHighest = SurfaceContainerHighestDark,
        outline = OutlineDark,
        outlineVariant = OutlineVariantDark,
        inverseSurface = InverseSurfaceDark,
        inverseOnSurface = InverseOnSurfaceDark,
        surfaceTint = SurfaceDark,
    )

private val LightExtendedColors =
    FreightExtendedColors(
        successColor = SuccessGreenLight,
        warningColor = WarningAmberLight,
        infoColor = InfoBlueLight,
        settingsColor = SettingsPurpleLight,
        onSuccessColor = SuccessOnColor,
        dialogScrimColor = DialogScrimLight,
        surfaceGray = Color(0xFFF3F3F3),
        borderOverlay = Color(0xFFF1F5F9),
        surfaceSuccessLight = Color(0xFFECFDF5),
        surfaceSuccessLighter = Color(0xFFF0FDF4),
    )

private val DarkExtendedColors =
    FreightExtendedColors(
        successColor = SuccessGreenDark,
        warningColor = WarningAmberDark,
        infoColor = InfoBlueDark,
        settingsColor = SettingsPurpleDark,
        onSuccessColor = SuccessOnColor,
        dialogScrimColor = DialogScrimDark,
        surfaceGray = Color(0xFF374151),
        borderOverlay = Color(0xFF374151),
        surfaceSuccessLight = Color(0xFF064E3B),
        surfaceSuccessLighter = Color(0xFFECFDF5),
    )

@Composable
fun Freight24Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    val extendedColors =
        when {
            darkTheme -> DarkExtendedColors
            else -> LightExtendedColors
        }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb() // Use background color for status bar to blend in
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    val extendedFontsMono = MaterialTheme.typography.bodyMedium.copy(fontFamily = monoSpace)

    CompositionLocalProvider(LocalFreightExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
