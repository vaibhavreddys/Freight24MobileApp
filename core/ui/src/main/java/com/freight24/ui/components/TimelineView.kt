package com.freight24.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class TimelineStatus {
    COMPLETED,
    ACTIVE,
    PENDING,
}

data class TimelineItem(
    val title: String,
    val description: String,
    val time: String,
    val status: TimelineStatus,
)

@Composable
fun TimelineView(
    items: List<TimelineItem>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            TimelineEventItem(
                item = item,
                isLast = index == items.lastIndex,
            )
        }
    }
}

@Composable
private fun TimelineEventItem(
    item: TimelineItem,
    isLast: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Timeline indicator
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val indicatorColor =
                when (item.status) {
                    TimelineStatus.ACTIVE -> Color(0xFF4CAF50)
                    TimelineStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                    TimelineStatus.PENDING -> MaterialTheme.colorScheme.outlineVariant
                }

            Box(
                modifier =
                    Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(indicatorColor),
                contentAlignment = Alignment.Center,
            ) {
                if (item.status == TimelineStatus.COMPLETED) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                } else if (item.status == TimelineStatus.ACTIVE) {
                    Box(
                        modifier =
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier =
                        Modifier
                            .width(2.dp)
                            .height(40.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant),
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Event details
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(bottom = if (!isLast) 16.dp else 0.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (item.status == TimelineStatus.ACTIVE) FontWeight.Bold else FontWeight.Medium,
                    color = if (item.status == TimelineStatus.PENDING) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = item.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
