package com.freight24.trucker.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.freight24.trucker.feature.profile.view.ProfileViewModel
import com.freight24.trucker.feature.trips.view.TripsScreen
import com.freight24.trucker.feature.trucks.view.TrucksScreen
import com.freight24.trucker.navigation.TruckerDestination
import com.freight24.ui.components.AppTopBar
import com.freight24.ui.components.FreightBottomNavScaffold

@Composable
fun TruckerHomeScreen(navController: NavHostController) {
    var selectedTab by rememberSaveable { mutableStateOf(TruckerTab.Loads) }

    val loadBoardViewModel: LoadBoardViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val isProfileEditing by profileViewModel.isEditing.collectAsState()

    // Read here only to decide whether back collapses a city card first. The
    // card list itself stays in LoadBoardScreen, which scrolls the collapsed
    // header back into view.
    val expandedCityId by loadBoardViewModel.expandedCityId.collectAsState()

    // HOIST SCROLL STATE
    val loadBoardListState = rememberLazyListState()
    val tripsListState = rememberLazyListState()

    val isProfileTab = selectedTab == TruckerTab.Profile
    val isLoadsTab = selectedTab == TruckerTab.Loads

    // Back handler, priority-ordered. Editing is a destructive mode, so back
    // cancels the edit rather than falling through to the tab switch that
    // would silently discard staged uploads. An expanded city card is the next
    // layer out: it owns a whole indent list, so back collapses it instead of
    // making the user scroll back up to the header to close it. Only when
    // neither is active does back fall back to the tab switch, and on the Loads
    // tab with nothing expanded it is left unhandled so the app exits.
    //
    // These conditions are mutually exclusive rather than layered, so the
    // last-registered-wins BackHandler ordering never has to be relied on:
    // the collapse handler needs the Loads tab, the tab switch needs anything
    // but the Loads tab.
    BackHandler(enabled = isProfileTab && isProfileEditing) {
        profileViewModel.cancelEdit()
    }
    BackHandler(enabled = isLoadsTab && expandedCityId != null) {
        loadBoardViewModel.collapseExpandedCity()
    }
    BackHandler(enabled = !isLoadsTab) {
        selectedTab = TruckerTab.Loads
    }

    FreightBottomNavScaffold(
        items = TruckerTab.entries,
        selectedItem = selectedTab,
        onItemSelected = { selectedTab = it },
        bottomBarEnabled = !(isProfileTab && isProfileEditing),
        topBar = {
            AppTopBar(
                // The title is the primary edit-mode signal now that the pencil
                // becomes a close affordance.
                title = if (isProfileTab && isProfileEditing) "Edit profile" else getFormattedTabName(selectedTab.id),
                showSearch = selectedTab.id == "loads",
                onSearchClick = loadBoardViewModel::onSearchClick,
                trailingActionIcon =
                    when {
                        !isProfileTab -> null
                        isProfileEditing -> Icons.Default.Close
                        else -> Icons.Default.Edit
                    },
                trailingActionContentDescription = if (isProfileEditing) "Close edit profile" else "Edit profile",
                onTrailingActionClick = {
                    if (isProfileTab) {
                        profileViewModel.toggleEditMode()
                    }
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

                TruckerTab.Profile ->
                    ProfileScreen(
                        paddingValues = paddingValues,
                        viewModel = profileViewModel,
                        onSettingsClick = {
                            navController.navigate(TruckerDestination.Settings.route)
                        },
                    )
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
