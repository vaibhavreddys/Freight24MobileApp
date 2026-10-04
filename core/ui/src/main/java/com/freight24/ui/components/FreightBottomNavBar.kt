package com.freight24.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun <T : NavTabItem> FreightBottomNavBar(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current

    NavigationBar(
        modifier = modifier,
        // The bar itself sits on the screen's surface; no tonal lift needed
        // because the selected indicator handles the brand accent.
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets.navigationBars,
    ) {
        items.forEach { item ->
            val selected = item.id == selectedItem.id

            // The active item wears the PRIMARY container, not secondary.
            // Secondary is the brand's brick-red accent in the Logistics
            // palette, which rendered the selected tab as a pink lozenge on a
            // forest-green bar. Primary/onPrimaryContainer keeps the selection
            // indicator on-brand in every palette.
            val disabledAlpha = 0.38f
            val muted = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = disabledAlpha)

            NavigationBarItem(
                selected = selected,
                enabled = enabled,
                onClick = {
                    onItemSelected(item)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                modifier = Modifier
                    .testTag("tab_${item.id}")
                    .semantics(mergeDescendants = true) {
                        testTagsAsResourceId = true
                        contentDescription = "tab_${item.id}"
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
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor =
                            if (enabled) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                muted
                            },
                        selectedTextColor =
                            if (enabled) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                muted
                            },
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor =
                            if (enabled) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                            },
                        disabledIconColor = muted,
                        disabledTextColor = muted,
                    ),
            )
        }
    }
}
