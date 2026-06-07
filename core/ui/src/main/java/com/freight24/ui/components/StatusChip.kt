package com.freight24.ui.components

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
import com.freight24.common.util.NameFormatter
import com.freight24.ui.theme.FreightThemeTokens

@Composable
fun StatusChip(status: String) {
    val tokens = FreightThemeTokens.colors
    val (backgroundColor, textColor) =
        when (status.uppercase()) {
            "ASSIGNED" -> tokens.surfaceInfo to tokens.onSurfaceInfo
            "LOADING", "IN_TRANSIT", "IN TRANSIT" -> tokens.surfaceWarning to tokens.onSurfaceWarning
            "UNLOADED", "INVOICED" -> tokens.surfaceSuccess to tokens.onSurfaceSuccess
            "PAID" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp),
    ) {
        Text(
            text = NameFormatter.formatTripStatusMessage(status),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
        )
    }
}
