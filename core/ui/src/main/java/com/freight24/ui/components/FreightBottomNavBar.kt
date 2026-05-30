package com.freight24.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun <T : NavTabItem> FreightBottomNavBar(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceTint,
        tonalElevation = 6.dp,
        windowInsets = WindowInsets.navigationBars,
    ) {
        items.forEach { item ->
            val selected = item.id == selectedItem.id

            NavigationBarItem(
                selected = selected,
                onClick = {
                    onItemSelected(item)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.titleRes),
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.titleRes),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.surface,
                    ),
            )
        }
    }
}
