package com.freight24.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier

/**
 * Viewport insets for a scrollable form, keyboard included.
 *
 * Apply to the scroll container, *before* `verticalScroll`:
 *
 * ```
 * Column(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .formViewportInsets(paddingValues)
 *         .verticalScroll(scrollState),
 * )
 * ```
 *
 * Why this exists: the app is edge-to-edge, so the IME is an overlay the layout
 * knows nothing about and `windowSoftInputMode="adjustResize"` is ignored on
 * Android 15+. A scroll container left to `fillMaxSize()` therefore spans the
 * full window height, *behind* the keyboard. Compose's bring-into-view then
 * measures the focused field against that phantom viewport, decides it is
 * "already visible", and never scrolls — so the field being typed into sits
 * under the keyboard with no way to reach it. Measured on device before the fix:
 * the Remarks field sat at y=1972 with the keyboard starting at y≈1560, and the
 * last visible field was at the exact same offset it held with the keyboard
 * closed: zero scroll.
 *
 * Order matters and is the whole trick:
 *
 *  - `imePadding()` **before** `verticalScroll` shrinks the viewport to the part
 *    of the screen the user can actually see. Putting it after would pad the
 *    scroll *content* instead, which is unbounded and therefore meaningless.
 *  - `consumeWindowInsets(paddingValues)` stops the scaffold's own insets being
 *    counted a second time; without it the IME inset is applied twice and the
 *    bottom of the form sits a navigation bar's height too low.
 *
 * A pinned action bar must be a sibling *below* the scroll container in normal
 * flow, not an overlay. An overlay leaves the viewport's bottom edge honest but
 * the bar covers the strip underneath it, and bring-into-view — which only knows
 * the scroll container's own bounds — will happily park the focused field in
 * exactly that strip. Giving the scroll container `weight(1f)` and the bar its
 * natural height makes the viewport end where the bar begins.
 */
fun Modifier.formViewportInsets(scaffoldPadding: PaddingValues): Modifier =
    padding(scaffoldPadding)
        .consumeWindowInsets(scaffoldPadding)
        .imePadding()
