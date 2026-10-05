package com.freight24.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.ui.theme.FreightThemeTokens

@Composable
fun DashedUploadCard(
    title: String,
    subtitle: String,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier,
    statusText: String? = null,
    isUploaded: Boolean = false,
    icon: ImageVector = Icons.Default.FileUpload,
    label: String? = null, // "Required" or "Optional"
    uploadTestTag: String? = null, // tags the clickable dashed upload area
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val extended = FreightThemeTokens.colors
        // Status-color mapping: uploaded → success palette, awaiting →
        // warning palette. Replaces the legacy "green/orange" hex codes
        // (Color(0xFF2E7D32) and Color(0xFFEF6C00)) so the chip harmonizes
        // with the rest of the M3 system in both light and dark mode.
        val uploadedStatusColor = extended.onSurfaceSuccess
        val pendingStatusColor = extended.onSurfaceWarning
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            if (label != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        val dashedColor = MaterialTheme.colorScheme.outlineVariant

        Surface(
            onClick = onUploadClick,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = null,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (uploadTestTag != null) {
                            Modifier
                                .testTag(uploadTestTag)
                                .semantics { testTagsAsResourceId = true }
                        } else {
                            Modifier
                        },
                    )
                    .drawBehind {
                        val stroke =
                            Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                            )
                        drawRoundRect(
                            color = dashedColor,
                            style = stroke,
                            cornerRadius = CornerRadius(12.dp.toPx()),
                        )
                    },
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.size(48.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isUploaded) Icons.Default.CheckCircle else icon,
                                contentDescription = null,
                                tint = if (isUploaded) extended.success else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (isUploaded) "File Selected" else title.replace("Policy", "Document").let { "Add $it" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (statusText != null) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Minor dot or icon
                Text(
                    text = "● Status: $statusText",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUploaded) uploadedStatusColor else pendingStatusColor,
                )
            }
        }
    }
}
