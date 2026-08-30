package com.freight24.ui.connect

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.freight24.ui.theme.Error30
import com.freight24.ui.theme.Error40
import com.freight24.ui.theme.Error80
import com.freight24.ui.theme.Error90
import com.freight24.ui.theme.FreightExtendedColors
import com.freight24.ui.theme.OnError100
import com.freight24.ui.theme.OnError20
import com.freight24.ui.theme.OnErrorContainer100
import com.freight24.ui.theme.OnPrimary20

// ---------------------------------------------------------------------------
// Connect light scheme
// ---------------------------------------------------------------------------

internal val ConnectLightColorScheme: ColorScheme =
    lightColorScheme(
        primary = ConnectPrimary40,
        onPrimary = ConnectOnPrimary100,
        primaryContainer = ConnectPrimary90,
        onPrimaryContainer = ConnectOnPrimaryContainer100,
        inversePrimary = ConnectInversePrimaryLight,
        secondary = ConnectSecondary40,
        onSecondary = ConnectOnSecondary100,
        secondaryContainer = ConnectSecondary90,
        onSecondaryContainer = ConnectOnSecondaryContainer100,
        tertiary = ConnectTertiary40,
        onTertiary = ConnectOnTertiary100,
        tertiaryContainer = ConnectTertiary90,
        onTertiaryContainer = ConnectOnTertiaryContainer100,
        error = Error40,
        onError = OnError100,
        errorContainer = Error90,
        onErrorContainer = OnErrorContainer100,
        background = ConnectBackgroundLight98,
        onBackground = ConnectOnBackgroundLight10,
        surface = ConnectSurfaceLight98,
        onSurface = ConnectOnSurfaceLight10,
        surfaceVariant = ConnectSurfaceVariantLight90,
        onSurfaceVariant = ConnectOnSurfaceVariantLight30,
        surfaceDim = ConnectSurfaceDimLight87,
        surfaceBright = ConnectSurfaceBrightLight98,
        surfaceContainerLowest = ConnectSurfaceContainerLowestLight100,
        surfaceContainerLow = ConnectSurfaceContainerLowLight96,
        surfaceContainer = ConnectSurfaceContainerLight94,
        surfaceContainerHigh = ConnectSurfaceContainerHighLight92,
        surfaceContainerHighest = ConnectSurfaceContainerHighestLight90,
        outline = ConnectOutlineLight50,
        outlineVariant = ConnectOutlineVariantLight80,
        inverseSurface = ConnectInverseSurfaceLight20,
        inverseOnSurface = ConnectInverseOnSurfaceLight95,
        surfaceTint = ConnectPrimary40,
    )

// ---------------------------------------------------------------------------
// Connect dark scheme
// ---------------------------------------------------------------------------

internal val ConnectDarkColorScheme: ColorScheme =
    darkColorScheme(
        primary = ConnectPrimary80,
        onPrimary = OnPrimary20,
        primaryContainer = ConnectPrimary30,
        onPrimaryContainer = ConnectPrimary90,
        inversePrimary = ConnectInversePrimaryDark,
        secondary = ConnectSecondary80,
        onSecondary = ConnectOnSecondary20,
        secondaryContainer = ConnectSecondary30,
        onSecondaryContainer = ConnectSecondary90,
        tertiary = ConnectTertiary80,
        onTertiary = ConnectOnTertiary20,
        tertiaryContainer = ConnectTertiary30,
        onTertiaryContainer = ConnectTertiary90,
        error = Error80,
        onError = OnError20,
        errorContainer = Error30,
        onErrorContainer = Error90,
        background = ConnectBackgroundDark6,
        onBackground = ConnectOnBackgroundDark90,
        surface = ConnectSurfaceDark6,
        onSurface = ConnectOnSurfaceDark90,
        surfaceVariant = ConnectSurfaceVariantDark30,
        onSurfaceVariant = ConnectOnSurfaceVariantDark80,
        surfaceDim = ConnectSurfaceDimDark6,
        surfaceBright = ConnectSurfaceBrightDark22,
        surfaceContainerLowest = ConnectSurfaceContainerLowestDark4,
        surfaceContainerLow = ConnectSurfaceContainerLowDark10,
        surfaceContainer = ConnectSurfaceContainerDark12,
        surfaceContainerHigh = ConnectSurfaceContainerHighDark17,
        surfaceContainerHighest = ConnectSurfaceContainerHighestDark20,
        outline = ConnectOutlineDark60,
        outlineVariant = ConnectOutlineVariantDark30,
        inverseSurface = ConnectInverseSurfaceDark90,
        inverseOnSurface = ConnectInverseOnSurfaceDark10,
        surfaceTint = ConnectPrimary80,
    )

// ---------------------------------------------------------------------------
// Extended semantic colors (FreightExtendedColors contract)
// ---------------------------------------------------------------------------

@Immutable
internal data class ConnectExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val surfaceSuccess: Color,
    val onSurfaceSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val surfaceWarning: Color,
    val onSurfaceWarning: Color,
    val info: Color,
    val onInfo: Color,
    val surfaceInfo: Color,
    val onSurfaceInfo: Color,
    val settings: Color,
    val onSettings: Color,
    val dialogScrim: Color,
    val routeOrigin: Color,
    val onRouteOrigin: Color,
    val routeDestination: Color,
    val onRouteDestination: Color,
    val statusAvailable: Color,
    val onStatusAvailable: Color,
    val statusPending: Color,
    val onStatusPending: Color,
) {
    fun toFreightExtendedColors() = FreightExtendedColors(
        success = success,
        onSuccess = onSuccess,
        surfaceSuccess = surfaceSuccess,
        onSurfaceSuccess = onSurfaceSuccess,
        warning = warning,
        onWarning = onWarning,
        surfaceWarning = surfaceWarning,
        onSurfaceWarning = onSurfaceWarning,
        info = info,
        onInfo = onInfo,
        surfaceInfo = surfaceInfo,
        onSurfaceInfo = onSurfaceInfo,
        settings = settings,
        onSettings = onSettings,
        dialogScrim = dialogScrim,
        routeOrigin = routeOrigin,
        onRouteOrigin = onRouteOrigin,
        routeDestination = routeDestination,
        onRouteDestination = onRouteDestination,
        statusAvailable = statusAvailable,
        onStatusAvailable = onStatusAvailable,
        statusPending = statusPending,
        onStatusPending = onStatusPending,
    )
}

internal val ConnectLightExtendedColors = ConnectExtendedColors(
    success = ConnectSuccessLight,
    onSuccess = ConnectOnSuccessLight,
    surfaceSuccess = ConnectSurfaceSuccessLight,
    onSurfaceSuccess = ConnectOnSurfaceSuccessLight,
    warning = ConnectWarningLight,
    onWarning = ConnectOnWarningLight,
    surfaceWarning = ConnectSurfaceWarningLight,
    onSurfaceWarning = ConnectOnSurfaceWarningLight,
    info = ConnectInfoLight,
    onInfo = ConnectOnInfoLight,
    surfaceInfo = ConnectSurfaceInfoLight,
    onSurfaceInfo = ConnectOnSurfaceInfoLight,
    settings = ConnectBrandForest,
    onSettings = Color.White,
    dialogScrim = Color.Black.copy(alpha = 0.55f),
    routeOrigin = ConnectPrimary40,
    onRouteOrigin = ConnectOnPrimary100,
    routeDestination = ConnectTertiary40,
    onRouteDestination = ConnectOnTertiary100,
    statusAvailable = ConnectSuccessLight,
    onStatusAvailable = ConnectOnSuccessLight,
    statusPending = ConnectWarningLight,
    onStatusPending = ConnectOnWarningLight,
)

internal val ConnectDarkExtendedColors = ConnectExtendedColors(
    success = ConnectSuccessDark,
    onSuccess = ConnectOnSuccessDark,
    surfaceSuccess = ConnectSurfaceSuccessDark,
    onSurfaceSuccess = ConnectOnSurfaceSuccessDark,
    warning = ConnectWarningDark,
    onWarning = ConnectOnWarningDark,
    surfaceWarning = ConnectSurfaceWarningDark,
    onSurfaceWarning = ConnectOnSurfaceWarningDark,
    info = ConnectInfoDark,
    onInfo = ConnectOnInfoDark,
    surfaceInfo = ConnectSurfaceInfoDark,
    onSurfaceInfo = ConnectOnSurfaceInfoDark,
    settings = ConnectPrimary80,
    onSettings = ConnectPrimary10,
    dialogScrim = Color.Black.copy(alpha = 0.65f),
    routeOrigin = ConnectPrimary80,
    onRouteOrigin = ConnectPrimary10,
    routeDestination = ConnectTertiary80,
    onRouteDestination = ConnectTertiary30,
    statusAvailable = ConnectSuccessDark,
    onStatusAvailable = ConnectOnSuccessDark,
    statusPending = ConnectWarningDark,
    onStatusPending = ConnectOnWarningDark,
)

/** Convenience accessor for the connect splash background per mode. */
internal fun connectSplashBackground(isDark: Boolean): Color =
    if (isDark) ConnectSplashBackgroundDark else ConnectSplashBackgroundLight
