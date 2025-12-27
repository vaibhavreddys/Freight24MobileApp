package com.freight24.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.freight24.core.ui.navigation.BottomNavItem

@Composable
fun FreightBottomNavScaffold(
    items: List<BottomNavItem>,
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues, BottomNavItem) -> Unit
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
                onItemSelected = onItemSelected
            )
        }
    ) { paddingValues ->
        content(paddingValues, selectedItem)
    }
}
