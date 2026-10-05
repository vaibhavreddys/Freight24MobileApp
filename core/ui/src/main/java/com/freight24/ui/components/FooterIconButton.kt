package com.freight24.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun FooterIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    highlighted: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val iconScale =
        animateFloatAsState(
            targetValue = if (highlighted) 1.15f else 1f,
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            label = "iconScale",
        )
    val iconTint =
        animateColorAsState(
            // Bookmarked = brick-red secondary: a sharp accent against the forest
            // primary Call button, so "interested" reads instantly and the two
            // actions never compete in the same hue (dominant + accent rule).
            targetValue = if (highlighted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
            animationSpec = tween(durationMillis = 220),
            label = "iconTint",
        )

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        // Same modifier chain as the caller's testTag so the tag, the
        // resourceId mapping and the selected state live on ONE semantics
        // node (Maestro selectors match id+selected on the same node).
        modifier = modifier.semantics { selected = highlighted },
    ) {
        IconButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
            modifier = Modifier.size(40.dp),
            enabled = enabled,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint.value,
                modifier = Modifier.size(18.dp).scale(iconScale.value),
            )
        }
    }
}
