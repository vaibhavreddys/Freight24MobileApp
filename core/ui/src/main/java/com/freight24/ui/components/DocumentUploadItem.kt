package com.freight24.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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

    object Uploading : DocumentStatus()

    object UploadError : DocumentStatus()

    data class Uploaded(
        val id: String,
        val label: String,
        val backgroundColor: Color,
        val textColor: Color,
        val canReupload: Boolean = false,
    ) : DocumentStatus()
}

@Composable
fun DocumentUploadItem(
    title: String,
    subtitle: String,
    status: DocumentStatus,
    onUploadClick: () -> Unit,
    onViewClick: (String) -> Unit = {},
    showStatusChip: Boolean = true,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(enabled = status is DocumentStatus.Uploaded) {
                    if (status is DocumentStatus.Uploaded) {
                        onViewClick(status.id)
                    }
                }
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (status) {
                is DocumentStatus.Pending -> {
                    IconButton(onClick = onUploadClick) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                is DocumentStatus.Uploading -> {
                    LinearProgressIndicator(
                        modifier = Modifier.width(88.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    IconButton(onClick = {}, enabled = false) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Uploading",
                            tint = MaterialTheme.colorScheme.outline,
                        )
                    }
                }

                is DocumentStatus.UploadError -> {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text = "Upload Failed",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                    IconButton(onClick = onUploadClick) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry Upload",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                is DocumentStatus.Uploaded -> {
                    if (showStatusChip) {
                        Surface(
                            onClick = { onViewClick(status.id) },
                            color = status.backgroundColor,
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Text(
                                text = status.label,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = status.textColor,
                            )
                        }
                    }

                    if (status.canReupload) {
                        IconButton(onClick = onUploadClick) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    if (trailingIcon != null) {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }
    }
}
