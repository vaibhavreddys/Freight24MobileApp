package com.freight24.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A reusable filter chip component for consistent selection UI across the app.
 * This matches the design used in the Trips tab but with reduced default sizes.
 */
@Composable
fun FilterSelectionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val borderColor = if (isSelected) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        modifier = modifier.clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) null else BorderStroke(1.dp, borderColor),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
        )
    }
}
