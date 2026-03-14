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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme =
    lightColorScheme(
        primary = PrimaryGreen,
        onPrimary = CardLight,
        secondary = SecondaryRed,
        onSecondary = CardLight,
        tertiary = PurpleTertiary,
        background = BackgroundLight,
        surface = CardLight,
        onBackground = TextMainLight,
        onSurface = TextMainLight,
        surfaceVariant = BackgroundLight, // Slightly different shade for variants
        onSurfaceVariant = TextMutedLight,
        surfaceDim = DisabledButtonColorLight,
        tertiaryFixed = WarningYellow,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = PrimaryGreen,
        onPrimary = CardDark,
        secondary = SecondaryRed,
        onSecondary = CardDark,
        tertiary = PurpleTertiary,
        background = BackgroundDark,
        surface = CardDark,
        onBackground = TextMainDark,
        onSurface = TextMainDark,
        surfaceVariant = BackgroundDark,
        onSurfaceVariant = TextMutedDark,
        surfaceDim = DisabledButtonColorDark,
        tertiaryFixed = WarningYellow,
    )

private val LightExtendedColors =
    FreightExtendedColors(
        successColor = SuccessGreenLight,
        warningColor = WarningAmberLight,
        infoColor = InfoBlueLight,
        onSuccessColor = SuccessOnColor,
        dialogScrimColor = DialogScrimLight,
    )

private val DarkExtendedColors =
    FreightExtendedColors(
        successColor = SuccessGreenDark,
        warningColor = WarningAmberDark,
        infoColor = InfoBlueDark,
        onSuccessColor = SuccessOnColor,
        dialogScrimColor = DialogScrimDark,
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

    CompositionLocalProvider(LocalFreightExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
