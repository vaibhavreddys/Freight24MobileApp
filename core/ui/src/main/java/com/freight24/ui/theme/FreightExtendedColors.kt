package com.freight24.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic utility colors layered on top of [androidx.compose.material3.ColorScheme].
 *
 * Every status / brand color is a container-pair: the base color used on
 * container surfaces (chips, badges, inline icons) plus an `on*` color
 * guaranteed to meet ≥ 4.5:1 contrast against it. A second pair is provided
 * for use on neutral surfaces (e.g. "success banner on [colorScheme.surface]").
 */
@Immutable
data class FreightExtendedColors(
    // Success — used on status chips, success banners, success icons
    val success: Color,
    val onSuccess: Color,
    val surfaceSuccess: Color,
    val onSurfaceSuccess: Color,
    // Warning — used on pending/attention states
    val warning: Color,
    val onWarning: Color,
    val surfaceWarning: Color,
    val onSurfaceWarning: Color,
    // Info — used on informational chips, links
    val info: Color,
    val onInfo: Color,
    val surfaceInfo: Color,
    val onSurfaceInfo: Color,
    // Brand extras
    val settings: Color,
    val onSettings: Color,
    // Scrim — overlay used behind modal bottom sheets & dialogs
    val dialogScrim: Color,
    // Route map — origin (pickup) and destination (drop) markers on the
    // card-level route indicator. Map to primary/tertiary in the schemes
    // so they harmonize with the rest of the system. Read these instead
    // of hard-coding colorScheme.primary / colorScheme.tertiary inside
    // route components — keeps a single place to retune.
    val routeOrigin: Color,
    val onRouteOrigin: Color,
    val routeDestination: Color,
    val onRouteDestination: Color,
    // StatusAvailable — semantic alias for "ready / available" states
    // (e.g. "1 load available" banner). Maps to the success palette in
    // both modes so the meaning is consistent.
    val statusAvailable: Color,
    val onStatusAvailable: Color,
    // StatusPending — semantic alias for "pending / awaiting" states
    // (e.g. a verification in flight). Maps to the warning palette.
    val statusPending: Color,
    val onStatusPending: Color,
)

internal val LocalFreightExtendedColors =
    staticCompositionLocalOf<FreightExtendedColors> {
        // Defaults are Color.Unspecified so previews & unprovided content see no
        // surprise colors bleeding in — explicit Freight24Theme wraps are the only
        // path to real values.
        FreightExtendedColors(
            success = Color.Unspecified,
            onSuccess = Color.Unspecified,
            surfaceSuccess = Color.Unspecified,
            onSurfaceSuccess = Color.Unspecified,
            warning = Color.Unspecified,
            onWarning = Color.Unspecified,
            surfaceWarning = Color.Unspecified,
            onSurfaceWarning = Color.Unspecified,
            info = Color.Unspecified,
            onInfo = Color.Unspecified,
            surfaceInfo = Color.Unspecified,
            onSurfaceInfo = Color.Unspecified,
            settings = Color.Unspecified,
            onSettings = Color.Unspecified,
            dialogScrim = Color.Unspecified,
            routeOrigin = Color.Unspecified,
            onRouteOrigin = Color.Unspecified,
            routeDestination = Color.Unspecified,
            onRouteDestination = Color.Unspecified,
            statusAvailable = Color.Unspecified,
            onStatusAvailable = Color.Unspecified,
            statusPending = Color.Unspecified,
            onStatusPending = Color.Unspecified,
        )
    }

object FreightThemeTokens {
    val colors: FreightExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFreightExtendedColors.current
}
