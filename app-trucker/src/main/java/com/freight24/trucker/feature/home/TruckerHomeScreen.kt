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

    // HOIST SCROLL STATE
    val loadBoardListState = rememberLazyListState()
    val tripsListState = rememberLazyListState()

    val isProfileTab = selectedTab == TruckerTab.Profile

    // Single back handler, priority-ordered. Editing is a destructive mode, so
    // back cancels the edit rather than falling through to the tab switch that
    // would silently discard staged uploads. Two separate BackHandlers would
    // both be enabled on the Profile tab while editing and rely on
    // last-registered-wins, which is too implicit to rely on.
    BackHandler(enabled = isProfileTab && isProfileEditing) {
        profileViewModel.cancelEdit()
    }
    BackHandler(enabled = selectedTab != TruckerTab.Loads) {
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
                onSearchClick = {
                    // TODO: trigger search
                },
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
