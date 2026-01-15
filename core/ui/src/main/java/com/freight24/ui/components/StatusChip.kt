package com.freight24.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.common.util.NameFormatter

@Composable
fun StatusChip(status: String) {
    val (backgroundColor, textColor) =
        when (status.uppercase()) {
            "ASSIGNED" -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
            "LOADING", "IN_TRANSIT", "IN TRANSIT" -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
            "UNLOADED", "INVOICED" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
            "PAID" -> Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
            else -> Color(0xFFF5F5F5) to Color(0xFF616161)
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
