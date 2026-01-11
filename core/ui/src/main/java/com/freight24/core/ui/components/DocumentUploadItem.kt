package com.freight24.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (status) {
                is DocumentStatus.Pending -> Icons.Default.FileUpload
                is DocumentStatus.Uploaded -> if (status.isVerified) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty
            }
            
            val iconBg = when (status) {
                is DocumentStatus.Pending -> MaterialTheme.colorScheme.primaryContainer
                is DocumentStatus.Uploaded -> if (status.isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
            }
            
            val iconTint = when (status) {
                is DocumentStatus.Pending -> MaterialTheme.colorScheme.primary
                is DocumentStatus.Uploaded -> if (status.isVerified) Color(0xFF2E7D32) else Color(0xFFEF6C00)
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (status is DocumentStatus.Pending) {
                IconButton(
                    onClick = onUploadClick,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Upload",
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else if (status is DocumentStatus.Uploaded) {
                StatusTag(
                    text = if (status.isVerified) "VERIFIED" else "Verification In Progress",
                    isSuccess = status.isVerified
                )
            }
        }
    }
}

@Composable
private fun StatusTag(text: String, isSuccess: Boolean) {
    val bgColor = if (isSuccess) Color.Transparent else Color(0xFFFFF9C4)
    val textColor = if (isSuccess) MaterialTheme.colorScheme.primary else Color(0xFFFBC02D)
    
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            if (isSuccess) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = textColor
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
