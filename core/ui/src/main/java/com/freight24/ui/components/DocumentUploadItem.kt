package com.freight24.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class DocumentStatus {
    object Pending : DocumentStatus()
    data class Uploaded(val id: String, val isVerified: Boolean) : DocumentStatus()
}

@Composable
fun DocumentUploadItem(
    title: String,
    subtitle: String,
    status: DocumentStatus,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when (status) {
            is DocumentStatus.Pending -> {
                IconButton(onClick = onUploadClick) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Upload",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            is DocumentStatus.Uploaded -> {
                Surface(
                    color = if (status.isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(
                        text = if (status.isVerified) "Verified" else "Pending",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (status.isVerified) Color(0xFF2E7D32) else Color(0xFFEF6C00),
                    )
                }
            }
        }
    }
}
