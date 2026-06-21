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

// ---------------------------------------------------------------------------
// Indigo M3 schemes (existing — preserved exactly as-is)
// ---------------------------------------------------------------------------

private val IndigoLightColorScheme =
    lightColorScheme(
        primary = Primary40,
        onPrimary = OnPrimary100,
        primaryContainer = Primary90,
        onPrimaryContainer = OnPrimaryContainer100,
        inversePrimary = InversePrimaryLight,
        secondary = Secondary40,
        onSecondary = OnSecondary100,
        secondaryContainer = Secondary90,
        onSecondaryContainer = OnSecondaryContainer100,
        tertiary = Tertiary40,
        onTertiary = OnTertiary100,
        tertiaryContainer = Tertiary90,
        onTertiaryContainer = OnTertiaryContainer100,
        error = Error40,
        onError = OnError100,
        errorContainer = Error90,
        onErrorContainer = OnErrorContainer100,
        background = BackgroundLight98,
        onBackground = OnBackgroundLight10,
        surface = SurfaceLight98,
        onSurface = OnSurfaceLight10,
        surfaceVariant = SurfaceVariantLight90,
        onSurfaceVariant = OnSurfaceVariantLight30,
        surfaceDim = SurfaceDimLight87,
        surfaceBright = SurfaceBrightLight98,
        surfaceContainerLowest = SurfaceContainerLowestLight100,
        surfaceContainerLow = SurfaceContainerLowLight96,
        surfaceContainer = SurfaceContainerLight94,
        surfaceContainerHigh = SurfaceContainerHighLight92,
        surfaceContainerHighest = SurfaceContainerHighestLight90,
        outline = OutlineLight50,
        outlineVariant = OutlineVariantLight80,
        inverseSurface = InverseSurfaceLight20,
        inverseOnSurface = InverseOnSurfaceLight95,
        surfaceTint = Primary40,
    )

private val IndigoDarkColorScheme =
    darkColorScheme(
        primary = Primary80,
        onPrimary = OnPrimary20,
        primaryContainer = Primary30,
        onPrimaryContainer = Primary90,
        inversePrimary = InversePrimaryDark,
        secondary = Secondary80,
        onSecondary = OnSecondary20,
        secondaryContainer = Secondary30,
        onSecondaryContainer = Secondary90,
        tertiary = Tertiary80,
        onTertiary = OnTertiary20,
        tertiaryContainer = Tertiary30,
        onTertiaryContainer = Tertiary90,
        error = Error80,
        onError = OnError20,
        errorContainer = Error30,
        onErrorContainer = Error90,
        background = BackgroundDark6,
        onBackground = OnBackgroundDark90,
        surface = SurfaceDark6,
        onSurface = OnSurfaceDark90,
        surfaceVariant = SurfaceVariantDark30,
        onSurfaceVariant = OnSurfaceVariantDark80,
        surfaceDim = SurfaceDimDark6,
        surfaceBright = SurfaceBrightDark22,
        surfaceContainerLowest = SurfaceContainerLowestDark4,
        surfaceContainerLow = SurfaceContainerLowDark10,
        surfaceContainer = SurfaceContainerDark12,
        surfaceContainerHigh = SurfaceContainerHighDark17,
        surfaceContainerHighest = SurfaceContainerHighestDark20,
        outline = OutlineDark60,
        outlineVariant = OutlineVariantDark30,
        inverseSurface = InverseSurfaceDark90,
        inverseOnSurface = InverseOnSurfaceDark10,
        surfaceTint = Primary80,
    )

// ---------------------------------------------------------------------------
// Green M3 schemes (derived from the original Freight24 green identity)
// ---------------------------------------------------------------------------

private val GreenLightColorScheme =
    lightColorScheme(
        primary = GreenPrimary40,
        onPrimary = GreenOnPrimary100,
        primaryContainer = GreenPrimary90,
        onPrimaryContainer = GreenOnPrimaryContainer100,
        inversePrimary = GreenInversePrimaryLight,
        secondary = GreenSecondary40,
        onSecondary = GreenOnSecondary100,
        secondaryContainer = GreenSecondary90,
        onSecondaryContainer = GreenOnSecondaryContainer100,
        tertiary = GreenTertiary40,
        onTertiary = GreenOnTertiary100,
        tertiaryContainer = GreenTertiary90,
        onTertiaryContainer = GreenOnTertiaryContainer100,
        error = GreenError40,
        onError = GreenOnError100,
        errorContainer = GreenError90,
        onErrorContainer = GreenOnErrorContainer100,
        background = GreenBackgroundLight98,
        onBackground = GreenOnBackgroundLight10,
        surface = GreenSurfaceLight98,
        onSurface = GreenOnSurfaceLight10,
        surfaceVariant = GreenSurfaceVariantLight90,
        onSurfaceVariant = GreenOnSurfaceVariantLight30,
        surfaceDim = GreenSurfaceDimLight87,
        surfaceBright = GreenSurfaceBrightLight98,
        surfaceContainerLowest = GreenSurfaceContainerLowestLight100,
        surfaceContainerLow = GreenSurfaceContainerLowLight96,
        surfaceContainer = GreenSurfaceContainerLight94,
        surfaceContainerHigh = GreenSurfaceContainerHighLight92,
        surfaceContainerHighest = GreenSurfaceContainerHighestLight90,
        outline = GreenOutlineLight50,
        outlineVariant = GreenOutlineVariantLight80,
        inverseSurface = GreenInverseSurfaceLight20,
        inverseOnSurface = GreenInverseOnSurfaceLight95,
        surfaceTint = GreenPrimary40,
    )

private val GreenDarkColorScheme =
    darkColorScheme(
        primary = GreenPrimary80,
        onPrimary = GreenOnPrimary20,
        primaryContainer = GreenPrimary30,
        onPrimaryContainer = GreenPrimary90,
        inversePrimary = GreenInversePrimaryDark,
        secondary = GreenSecondary80,
        onSecondary = GreenOnSecondary20,
        secondaryContainer = GreenSecondary30,
        onSecondaryContainer = GreenSecondary90,
        tertiary = GreenTertiary80,
        onTertiary = GreenOnTertiary20,
        tertiaryContainer = GreenTertiary30,
        onTertiaryContainer = GreenTertiary90,
        error = GreenError80,
        onError = GreenOnError20,
        errorContainer = GreenError30,
        onErrorContainer = GreenError90,
        background = GreenBackgroundDark6,
        onBackground = GreenOnBackgroundDark90,
        surface = GreenSurfaceDark6,
        onSurface = GreenOnSurfaceDark90,
        surfaceVariant = GreenSurfaceVariantDark30,
        onSurfaceVariant = GreenOnSurfaceVariantDark80,
        surfaceDim = GreenSurfaceDimDark6,
        surfaceBright = GreenSurfaceBrightDark22,
        surfaceContainerLowest = GreenSurfaceContainerLowestDark4,
        surfaceContainerLow = GreenSurfaceContainerLowDark10,
        surfaceContainer = GreenSurfaceContainerDark12,
        surfaceContainerHigh = GreenSurfaceContainerHighDark17,
        surfaceContainerHighest = GreenSurfaceContainerHighestDark20,
        outline = GreenOutlineDark60,
        outlineVariant = GreenOutlineVariantDark30,
        inverseSurface = GreenInverseSurfaceDark90,
        inverseOnSurface = GreenInverseOnSurfaceDark10,
        surfaceTint = GreenPrimary80,
    )

// ---------------------------------------------------------------------------
// Logistics M3 schemes (forest green primary + brick red secondary,
// deep muted register. Status colors stay universal — see FreightExtendedColors.)
// ---------------------------------------------------------------------------

private val LogisticsLightColorScheme =
    lightColorScheme(
        primary = LogisticsPrimary40,
        onPrimary = LogisticsOnPrimary100,
        primaryContainer = LogisticsPrimary90,
        onPrimaryContainer = LogisticsOnPrimaryContainer100,
        inversePrimary = LogisticsInversePrimaryLight,
        secondary = LogisticsSecondary40,
        onSecondary = LogisticsOnSecondary100,
        secondaryContainer = LogisticsSecondary90,
        onSecondaryContainer = LogisticsOnSecondaryContainer100,
        tertiary = LogisticsTertiary40,
        onTertiary = LogisticsOnTertiary100,
        tertiaryContainer = LogisticsTertiary90,
        onTertiaryContainer = LogisticsOnTertiaryContainer100,
        error = LogisticsError40,
        onError = LogisticsOnError100,
        errorContainer = LogisticsError90,
        onErrorContainer = LogisticsOnErrorContainer100,
        background = LogisticsBackgroundLight98,
        onBackground = LogisticsOnBackgroundLight10,
        surface = LogisticsSurfaceLight98,
        onSurface = LogisticsOnSurfaceLight10,
        surfaceVariant = LogisticsSurfaceVariantLight90,
        onSurfaceVariant = LogisticsOnSurfaceVariantLight30,
        surfaceDim = LogisticsSurfaceDimLight87,
        surfaceBright = LogisticsSurfaceBrightLight98,
        surfaceContainerLowest = LogisticsSurfaceContainerLowestLight100,
        surfaceContainerLow = LogisticsSurfaceContainerLowLight96,
        surfaceContainer = LogisticsSurfaceContainerLight94,
        surfaceContainerHigh = LogisticsSurfaceContainerHighLight92,
        surfaceContainerHighest = LogisticsSurfaceContainerHighestLight90,
        outline = LogisticsOutlineLight50,
        outlineVariant = LogisticsOutlineVariantLight80,
        inverseSurface = LogisticsInverseSurfaceLight20,
        inverseOnSurface = LogisticsInverseOnSurfaceLight95,
        surfaceTint = LogisticsPrimary40,
    )

private val LogisticsDarkColorScheme =
    darkColorScheme(
        primary = LogisticsPrimary80,
        onPrimary = LogisticsOnPrimary20,
        primaryContainer = LogisticsPrimary30,
        onPrimaryContainer = LogisticsPrimary90,
        inversePrimary = LogisticsInversePrimaryDark,
        secondary = LogisticsSecondary80,
        onSecondary = LogisticsOnSecondary20,
        secondaryContainer = LogisticsSecondary30,
        onSecondaryContainer = LogisticsSecondary90,
        tertiary = LogisticsTertiary80,
        onTertiary = LogisticsOnTertiary20,
        tertiaryContainer = LogisticsTertiary30,
        onTertiaryContainer = LogisticsTertiary90,
        error = LogisticsError80,
        onError = LogisticsOnError20,
        errorContainer = LogisticsError30,
        onErrorContainer = LogisticsError90,
        background = LogisticsBackgroundDark6,
        onBackground = LogisticsOnBackgroundDark90,
        surface = LogisticsSurfaceDark6,
        onSurface = LogisticsOnSurfaceDark90,
        surfaceVariant = LogisticsSurfaceVariantDark30,
        onSurfaceVariant = LogisticsOnSurfaceVariantDark80,
        surfaceDim = LogisticsSurfaceDimDark6,
        surfaceBright = LogisticsSurfaceBrightDark22,
        surfaceContainerLowest = LogisticsSurfaceContainerLowestDark4,
        surfaceContainerLow = LogisticsSurfaceContainerLowDark10,
        surfaceContainer = LogisticsSurfaceContainerDark12,
        surfaceContainerHigh = LogisticsSurfaceContainerHighDark17,
        surfaceContainerHighest = LogisticsSurfaceContainerHighestDark20,
        outline = LogisticsOutlineDark60,
        outlineVariant = LogisticsOutlineVariantDark30,
        inverseSurface = LogisticsInverseSurfaceDark90,
        inverseOnSurface = LogisticsInverseOnSurfaceDark10,
        surfaceTint = LogisticsPrimary80,
    )

// ---------------------------------------------------------------------------
// Extended colors — Indigo (success/warning/info stay the same; brand
// accents routeOrigin/routeDestination swap to match the primary palette)
// ---------------------------------------------------------------------------

private val IndigoLightExtendedColors =
    FreightExtendedColors(
        success = SuccessLight,
        onSuccess = OnSuccessLight,
        surfaceSuccess = SurfaceSuccessLight,
        onSurfaceSuccess = OnSurfaceSuccessLight,
        warning = WarningLight,
        onWarning = OnWarningLight,
        surfaceWarning = SurfaceWarningLight,
        onSurfaceWarning = OnSurfaceWarningLight,
        info = InfoLight,
        onInfo = OnInfoLight,
        surfaceInfo = SurfaceInfoLight,
        onSurfaceInfo = OnSurfaceInfoLight,
        settings = SettingsLight,
        onSettings = OnSettingsLight,
        dialogScrim = DialogScrimLight,
        routeOrigin = Primary40,
        onRouteOrigin = OnPrimary100,
        routeDestination = Tertiary40,
        onRouteDestination = OnTertiary100,
        statusAvailable = SuccessLight,
        onStatusAvailable = OnSuccessLight,
        statusPending = WarningLight,
        onStatusPending = OnWarningLight,
    )

private val IndigoDarkExtendedColors =
    FreightExtendedColors(
        success = SuccessDark,
        onSuccess = OnSuccessDark,
        surfaceSuccess = SurfaceSuccessDark,
        onSurfaceSuccess = OnSurfaceSuccessDark,
        warning = WarningDark,
        onWarning = OnWarningDark,
        surfaceWarning = SurfaceWarningDark,
        onSurfaceWarning = OnSurfaceWarningDark,
        info = InfoDark,
        onInfo = OnInfoDark,
        surfaceInfo = SurfaceInfoDark,
        onSurfaceInfo = OnSurfaceInfoDark,
        settings = SettingsDark,
        onSettings = OnSettingsDark,
        dialogScrim = DialogScrimDark,
        routeOrigin = Primary80,
        onRouteOrigin = OnPrimary20,
        routeDestination = Tertiary80,
        onRouteDestination = OnTertiary20,
        statusAvailable = SuccessDark,
        onStatusAvailable = OnSuccessDark,
        statusPending = WarningDark,
        onStatusPending = OnWarningDark,
    )

// ---------------------------------------------------------------------------
// Extended colors — Green (success/warning/info stay the same; routeOrigin
// swaps to green primary so the route indicator harmonizes with the palette)
// ---------------------------------------------------------------------------

private val GreenLightExtendedColors =
    FreightExtendedColors(
        success = SuccessLight,
        onSuccess = OnSuccessLight,
        surfaceSuccess = SurfaceSuccessLight,
        onSurfaceSuccess = OnSurfaceSuccessLight,
        warning = WarningLight,
        onWarning = OnWarningLight,
        surfaceWarning = SurfaceWarningLight,
        onSurfaceWarning = OnSurfaceWarningLight,
        info = InfoLight,
        onInfo = OnInfoLight,
        surfaceInfo = SurfaceInfoLight,
        onSurfaceInfo = OnSurfaceInfoLight,
        settings = SettingsLight,
        onSettings = OnSettingsLight,
        dialogScrim = DialogScrimLight,
        routeOrigin = GreenPrimary40,
        onRouteOrigin = GreenOnPrimary100,
        routeDestination = GreenTertiary40,
        onRouteDestination = GreenOnTertiary100,
        statusAvailable = SuccessLight,
        onStatusAvailable = OnSuccessLight,
        statusPending = WarningLight,
        onStatusPending = OnWarningLight,
    )

private val GreenDarkExtendedColors =
    FreightExtendedColors(
        success = SuccessDark,
        onSuccess = OnSuccessDark,
        surfaceSuccess = SurfaceSuccessDark,
        onSurfaceSuccess = OnSurfaceSuccessDark,
        warning = WarningDark,
        onWarning = OnWarningDark,
        surfaceWarning = SurfaceWarningDark,
        onSurfaceWarning = OnSurfaceWarningDark,
        info = InfoDark,
        onInfo = OnInfoDark,
        surfaceInfo = SurfaceInfoDark,
        onSurfaceInfo = OnSurfaceInfoDark,
        settings = SettingsDark,
        onSettings = OnSettingsDark,
        dialogScrim = DialogScrimDark,
        routeOrigin = GreenPrimary80,
        onRouteOrigin = GreenOnPrimary20,
        routeDestination = GreenTertiary80,
        onRouteDestination = GreenOnTertiary20,
        statusAvailable = SuccessDark,
        onStatusAvailable = OnSuccessDark,
        statusPending = WarningDark,
        onStatusPending = OnWarningDark,
    )

// ---------------------------------------------------------------------------
// Extended colors — Logistics (forest + brick; success/warning/info stay
// universal; routeOrigin = green primary, routeDestination = brick red
// secondary for clear pickup/drop visual distinction)
// ---------------------------------------------------------------------------

private val LogisticsLightExtendedColors =
    FreightExtendedColors(
        success = SuccessLight,
        onSuccess = OnSuccessLight,
        surfaceSuccess = SurfaceSuccessLight,
        onSurfaceSuccess = OnSurfaceSuccessLight,
        warning = WarningLight,
        onWarning = OnWarningLight,
        surfaceWarning = SurfaceWarningLight,
        onSurfaceWarning = OnSurfaceWarningLight,
        info = InfoLight,
        onInfo = OnInfoLight,
        surfaceInfo = SurfaceInfoLight,
        onSurfaceInfo = OnSurfaceInfoLight,
        settings = LogisticsSettingsLight,
        onSettings = LogisticsOnSettingsLight,
        dialogScrim = DialogScrimLight,
        routeOrigin = LogisticsPrimary40,
        onRouteOrigin = LogisticsOnPrimary100,
        routeDestination = LogisticsSecondary40,
        onRouteDestination = LogisticsOnSecondary100,
        statusAvailable = SuccessLight,
        onStatusAvailable = OnSuccessLight,
        statusPending = WarningLight,
        onStatusPending = OnWarningLight,
    )

private val LogisticsDarkExtendedColors =
    FreightExtendedColors(
        success = SuccessDark,
        onSuccess = OnSuccessDark,
        surfaceSuccess = SurfaceSuccessDark,
        onSurfaceSuccess = OnSurfaceSuccessDark,
        warning = WarningDark,
        onWarning = OnWarningDark,
        surfaceWarning = SurfaceWarningDark,
        onSurfaceWarning = OnSurfaceWarningDark,
        info = InfoDark,
        onInfo = OnInfoDark,
        surfaceInfo = SurfaceInfoDark,
        onSurfaceInfo = OnSurfaceInfoDark,
        settings = LogisticsSettingsDark,
        onSettings = LogisticsOnSettingsDark,
        dialogScrim = DialogScrimDark,
        routeOrigin = LogisticsPrimary80,
        onRouteOrigin = LogisticsOnPrimary20,
        routeDestination = LogisticsSecondary80,
        onRouteDestination = LogisticsOnSecondary20,
        statusAvailable = SuccessDark,
        onStatusAvailable = OnSuccessDark,
        statusPending = WarningDark,
        onStatusPending = OnWarningDark,
    )

// ---------------------------------------------------------------------------
// CompositionLocal for the current theme choice
// ---------------------------------------------------------------------------

/** Provides the currently-selected [FreightAppTheme] throughout the tree. */
val LocalAppTheme =
    androidx.compose.runtime.staticCompositionLocalOf<FreightAppTheme> {
        FreightAppTheme.LOGISTICS
    }

/**
 * App-wide theme entry point.
 *
 * @param appTheme The user-selected color scheme (Indigo, Green, or Logistics).
 * @param darkTheme Whether dark mode is active. Derived from system when
 *   [darkModeOverride] is `"system"` (the default); forced on/off otherwise.
 * @param darkModeOverride One of `"system"`, `"on"`, or `"off"` — persisted
 *   by [com.freight24.common.manager.ThemePreferenceManager].
 * @param dynamicColor Material You dynamic color — opt-in only, default false.
 * @param content Root composable.
 */
@Composable
fun Freight24Theme(
    appTheme: FreightAppTheme = FreightAppTheme.LOGISTICS,
    darkTheme: Boolean = isSystemInDarkTheme(),
    darkModeOverride: String = "system",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Resolve dark mode: if the user picked "on"/"off", honour that;
    // otherwise defer to the system setting (the darkTheme parameter).
    val isDark = when (darkModeOverride) {
        "on" -> true
        "off" -> false
        else -> darkTheme
    }

    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            isDark && appTheme == FreightAppTheme.LOGISTICS -> LogisticsDarkColorScheme
            isDark && appTheme == FreightAppTheme.GREEN -> GreenDarkColorScheme
            isDark -> IndigoDarkColorScheme
            appTheme == FreightAppTheme.LOGISTICS -> LogisticsLightColorScheme
            appTheme == FreightAppTheme.GREEN -> GreenLightColorScheme
            else -> IndigoLightColorScheme
        }

    val extendedColors =
        when {
            isDark && appTheme == FreightAppTheme.LOGISTICS -> LogisticsDarkExtendedColors
            isDark && appTheme == FreightAppTheme.GREEN -> GreenDarkExtendedColors
            isDark -> IndigoDarkExtendedColors
            appTheme == FreightAppTheme.LOGISTICS -> LogisticsLightExtendedColors
            appTheme == FreightAppTheme.GREEN -> GreenLightExtendedColors
            else -> IndigoLightExtendedColors
        }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    CompositionLocalProvider(
        LocalFreightExtendedColors provides extendedColors,
        LocalAppTheme provides appTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
