package com.freight24.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String = "Loads",
    showSearch: Boolean = false,
    onSearchClick: (() -> Unit)? = null,
    showFilter: Boolean = false,
    onFilterClick: (() -> Unit)? = null,
    trailingActionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
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
                IconButton(onClick = onFilterClick) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                    )
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
                containerColor = MaterialTheme.colorScheme.surfaceTint,
            ),
    )
}
