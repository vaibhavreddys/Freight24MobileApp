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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.freight24.trucker.enums.TruckerTab
import com.freight24.trucker.feature.loads.LoadBoardScreen
import com.freight24.trucker.feature.loads.LoadBoardViewModel
import com.freight24.trucker.feature.profile.view.ProfileScreen
import com.freight24.trucker.feature.trips.view.TripsScreen
import com.freight24.trucker.feature.trucks.view.TrucksScreen
import com.freight24.trucker.navigation.TruckerDestination
import com.freight24.ui.components.AppTopBar
import com.freight24.ui.components.FreightBottomNavScaffold

@Composable
fun TruckerHomeScreen(navController: NavHostController) {
    var selectedTab by rememberSaveable { mutableStateOf(TruckerTab.Loads) }

    val loadBoardViewModel: LoadBoardViewModel = hiltViewModel()

    // 👇 HOIST SCROLL STATE
    val loadBoardListState = rememberLazyListState()
    val tripsListState = rememberLazyListState()

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
                        navController = navController,
                        onTruckClick = { truckId ->
                            navController.navigate(TruckerDestination.TruckDetail.createRoute(truckId))
                        },
                        onOnboardClick = {
                            navController.navigate(TruckerDestination.OnboardTruck.route)
                        },
                    )

                TruckerTab.Trips ->
                    TripsScreen(
                        paddingValues = paddingValues,
                        listState = tripsListState,
                        onTripClick = { tripId ->
                            navController.navigate(TruckerDestination.TripDetail.createRoute(tripId))
                        },
                    )

                TruckerTab.Profile -> ProfileScreen(paddingValues = paddingValues)
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
