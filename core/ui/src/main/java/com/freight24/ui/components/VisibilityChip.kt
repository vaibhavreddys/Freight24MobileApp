package com.freight24.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freight24.ui.theme.FreightThemeTokens

@Composable
fun VisibilityChip(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    // "Visible" reads as a positive state → success palette; "Hidden" is a
    // blocking / concerning state → error palette. Both come from the
    // extended/theme tokens so the chip flips with the rest of the theme
    // in dark mode.
    val extended = FreightThemeTokens.colors
    val backgroundColor =
        if (isVisible) extended.surfaceSuccess else MaterialTheme.colorScheme.errorContainer
    val contentColor =
        if (isVisible) extended.onSurfaceSuccess else MaterialTheme.colorScheme.onErrorContainer
    val text = if (isVisible) "VISIBLE" else "HIDDEN"
    val icon = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50), // Fully rounded corners
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )

            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
