package com.freight24.trucker.feature.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.trucker.enums.TruckerTab
import com.freight24.trucker.feature.loads.LoadBoardScreen
import com.freight24.trucker.feature.loads.LoadBoardViewModel
import com.freight24.trucker.feature.trips.view.TripsScreen
import com.freight24.trucker.feature.trucks.TrucksScreen
import com.freight24.ui.components.AppTopBar
import com.freight24.ui.components.FreightBottomNavScaffold

@Composable
fun TruckerHomeScreen() {
    var selectedTab by remember { mutableStateOf(TruckerTab.Loads) }

    val loadBoardViewModel: LoadBoardViewModel = hiltViewModel()

    // 👇 HOIST SCROLL STATE
    val loadBoardListState = rememberLazyListState()

    FreightBottomNavScaffold(
        items = TruckerTab.entries,
        selectedItem = selectedTab,
        onItemSelected = { selectedTab = it },
        topBar = {
            AppTopBar(
                title = getFormattedTabName(selectedTab.id),
                showSearch = selectedTab.id == "loads",
                onSearchClick = {
                    // TODO: trigger search
                },
            )
        },
    ) { paddingValues, tab ->

        Crossfade(targetState = tab, label = "TabTransition") { currentTab ->
            when (currentTab) {
                TruckerTab.Loads ->
                    LoadBoardScreen(
                        paddingValues = paddingValues,
                        viewModel = loadBoardViewModel,
                        listState = loadBoardListState,
                    )

                TruckerTab.Trucks ->
                    TrucksScreen(
                        paddingValues = paddingValues,
                    )

                TruckerTab.Trips -> TripsScreen(paddingValues = paddingValues)

                TruckerTab.Profile -> PlaceholderScreen("Profile", PaddingValues(0.dp))
            }
        }
    }
}

/*
  Returns a neatly formatted name for the tab.
  @input name: String
  @output String
  Eg. "loads" -> "Loads"
 */
fun getFormattedTabName(name: String): String =
    when (name) {
        "trips" -> "My Trips"
        else -> name.replaceFirstChar { it.uppercase() }
    }

@Composable
fun PlaceholderScreen(
    title: String,
    paddingValues: PaddingValues,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
        contentAlignment = Alignment.Center,
    ) {
        Text(title)
    }
}
