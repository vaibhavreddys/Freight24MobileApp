package com.freight24.trucker.feature.main

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freight24.core.ui.components.FreightBottomNavScaffold
import com.freight24.core.ui.components.LoadBoardTopBar
import com.freight24.core.ui.navigation.BottomNavItem
import com.freight24.trucker.feature.loads.LoadBoardScreen
import com.freight24.trucker.feature.loads.LoadBoardViewModel
import com.freight24.trucker.navigation.TruckerTabs

@Composable
fun TruckerMainScreen() {
    var selectedTab by remember { mutableStateOf(TruckerTabs.first()) }

    val loadBoardViewModel: LoadBoardViewModel = viewModel()

    // 👇 HOIST SCROLL STATE
    val loadBoardListState = rememberLazyListState()

    FreightBottomNavScaffold(
        items = TruckerTabs,
        selectedItem = selectedTab,
        onItemSelected = { selectedTab = it },
        topBar = {
            if (selectedTab.id == "loads") {
                LoadBoardTopBar()
            }
        }
    ) { paddingValues, tab ->

        when (tab.id) {
            "loads" -> LoadBoardScreen(
                paddingValues = paddingValues,
                viewModel = loadBoardViewModel,
                listState = loadBoardListState
            )
            "trips" -> PlaceholderScreen("Trips", paddingValues)
            "trucks" -> PlaceholderScreen("Trucks", paddingValues)
            "profile" -> PlaceholderScreen("Profile", paddingValues)
        }
    }
}


@Composable
fun PlaceholderScreen(
    title: String,
    paddingValues: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Text(title)
    }
}
