package com.freight24.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

interface NavTabItem {
    val id: String
    val titleRes: Int
    val icon: ImageVector
}

@Composable
fun <T : NavTabItem> FreightBottomNavScaffold(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues, T) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            topBar?.invoke()
        },
        bottomBar = {
            FreightBottomNavBar(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = onItemSelected,
            )
        },
    ) { paddingValues ->
        content(paddingValues, selectedItem)
    }
}
