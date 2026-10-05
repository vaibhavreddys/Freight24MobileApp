package com.freight24.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String = "Loads",
    showSearch: Boolean = false,
    onSearchClick: (() -> Unit)? = null,
    showFilter: Boolean = false,
    onFilterClick: (() -> Unit)? = null,
    filterBadge: Boolean = false,
    filterLabel: (@Composable () -> Unit)? = null,
    trailingActionIcon: ImageVector? = null,
    trailingActionContentDescription: String? = null,
    onTrailingActionClick: (() -> Unit)? = null,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        actions = {
            if (showSearch && onSearchClick != null) {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                    )
                }
            }
            if (showFilter && onFilterClick != null) {
                filterLabel?.invoke()
                Box {
                    IconButton(onClick = onFilterClick) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                        )
                    }
                    if (filterBadge) {
                        PulsingBadgeDot(
                            modifier =
                                Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = (5).dp),
                        )
                    }
                }
            }
            if (trailingActionIcon != null && onTrailingActionClick != null) {
                IconButton(onClick = onTrailingActionClick) {
                    Icon(
                        imageVector = trailingActionIcon,
                        contentDescription = trailingActionContentDescription,
                    )
                }
            }
        },
        windowInsets = WindowInsets.statusBars,
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
    )
}

@Composable
private fun PulsingBadgeDot(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "FilterBadgePulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 700),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "FilterBadgeAlpha",
    )
    Box(
        modifier =
            modifier
                .size(8.dp)
                .alpha(alpha)
                .background(MaterialTheme.colorScheme.error, CircleShape),
    )
}
