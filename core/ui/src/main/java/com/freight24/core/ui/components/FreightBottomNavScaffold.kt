package com.freight24.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.freight24.core.ui.navigation.BottomNavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreightBottomNavScaffold(
    items: List<BottomNavItem>,
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    topBar: @Composable () -> Unit = {},
    snackbarHostState: androidx.compose.material3.SnackbarHostState = remember { androidx.compose.material3.SnackbarHostState() },
    content: @Composable (PaddingValues, BottomNavItem) -> Unit
) {
    Scaffold(
        topBar = topBar,
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
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
