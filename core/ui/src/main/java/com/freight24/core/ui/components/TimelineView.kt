package com.freight24.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class TimelineStatus {
    COMPLETED, ACTIVE, PENDING
}

data class TimelineItem(
    val title: String,
    val description: String,
    val time: String?,
    val status: TimelineStatus
)

@Composable
fun TimelineView(
    items: List<TimelineItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        items.forEachIndexed { index, item ->
            TimelineRow(
                item = item,
                isLast = index == items.size - 1
            )
        }
    }
}

@Composable
private fun TimelineRow(
    item: TimelineItem,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            TimelineDot(status = item.status)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(
                            if (item.status == TimelineStatus.COMPLETED) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (item.status == TimelineStatus.PENDING) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else MaterialTheme.colorScheme.onSurface
                )
                
                if (item.time != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = item.time,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TimelineDot(status: TimelineStatus) {
    val containerColor = when (status) {
        TimelineStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        TimelineStatus.ACTIVE -> Color.Transparent
        TimelineStatus.PENDING -> MaterialTheme.colorScheme.outlineVariant
    }

    Box(
        modifier = Modifier
            .size(16.dp)
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (status == TimelineStatus.ACTIVE) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(if (status == TimelineStatus.COMPLETED) 16.dp else 12.dp)
                    .clip(CircleShape)
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                if (status == TimelineStatus.COMPLETED) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
