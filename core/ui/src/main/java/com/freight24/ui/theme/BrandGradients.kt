package com.freight24.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush

/**
 * Brand gradient derived from the current theme primary — a deep-to-soft
 * green fade that matches the Mine/All filter chips while staying
 * theme-aware (works in Indigo, Green and Logistics schemes, light & dark).
 */
@Composable
@ReadOnlyComposable
fun freightBrandGradient(): Brush {
    val primary = MaterialTheme.colorScheme.primary
    return Brush.linearGradient(
        colors = listOf(primary, primary.copy(alpha = 0.78f)),
    )
}
