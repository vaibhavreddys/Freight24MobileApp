package com.freight24.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Brand-standard TopAppBar with the Freight24 indigo color treatment.
 *
 * Use this everywhere instead of the raw M3 TopAppBar. The default
 * colors come from [freightTopAppBarColors] — pass a custom `colors`
 * only when a specific screen has a documented reason to deviate.
 *
 * Variants mirror the four M3 TopAppBar types:
 *   - FreightTopAppBar               (small, left-aligned)
 *   - FreightCenterAlignedTopAppBar  (small, center-aligned)
 *   - FreightMediumTopAppBar         (M3 medium expanded)
 *   - FreightLargeTopAppBar          (M3 large expanded)
 *
 * Adding a new variant? Mirror the signature of the underlying M3
 * composable exactly and default `colors` to [freightTopAppBarColors].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreightTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = freightTopAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreightCenterAlignedTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = freightTopAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    CenterAlignedTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreightMediumTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = freightTopAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    MediumTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreightLargeTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = freightTopAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    LargeTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior,
    )
}

/**
 * Brand-standard TopAppBar colors — clean, surface-anchored.
 *
 * The bar sits on a neutral surface so the screen content (cards,
 * the bottom nav, status chips) carries the brand color. Title,
 * nav icon, and action icons all use [onSurface] for solid legibility
 * on both light and dark surfaces.
 *
 * containerColor          = surface (no tonal lift on rest)
 * scrolledContainerColor  = surfaceContainer (subtle lift on scroll)
 * navigationIconContentColor / titleContentColor / actionIconContentColor
 *                          = onSurface
 *
 * Override per-screen by passing a custom `colors` argument — the
 * [FreightTopAppBar] etc. variants all accept one.
 *
 * For a softer brand presence (e.g. onboarding, splash, hero),
 * pass `containerColor = colorScheme.primaryContainer` and
 * `contentColor = colorScheme.onPrimaryContainer` — that's the
 * intended "brand wash" path, not a default.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun freightTopAppBarColors(): TopAppBarColors =
    TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
    )
