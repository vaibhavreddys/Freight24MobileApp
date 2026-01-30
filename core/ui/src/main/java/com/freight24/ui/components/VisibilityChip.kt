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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun VisibilityChip(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isVisible) Color(0xFFE6F4EA) else Color(0xFFFDE7E7)
    val contentColor = if (isVisible) Color(0xFF1A7A3A) else Color(0xFFD32F2F)
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
