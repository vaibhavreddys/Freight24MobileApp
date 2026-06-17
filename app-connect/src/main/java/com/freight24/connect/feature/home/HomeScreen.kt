package com.freight24.connect.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.freight24.connect.R
import com.freight24.connect.enum.ConnectTab
import com.freight24.connect.feature.dashboard.views.DashboardScreen
import com.freight24.connect.feature.indents.views.IndentsScreen
import com.freight24.connect.feature.trips.views.TripsScreen
import com.freight24.connect.feature.findTrucks.views.FindTrucksScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.freight24.connect.feature.trips.domain.usecase.TripTypes
import com.freight24.connect.navigation.ConnectDestination
import com.freight24.ui.components.AppTopBar
import com.freight24.ui.components.FreightBottomNavScaffold

@Composable
internal fun HomeScreen(
    onCreateIndentClick: () -> Unit,
    onIndentClick: (String) -> Unit,
    onCloneClick: (String, String) -> Unit,
    onSearchClick: () -> Unit = {},
    onTripClick: (String) -> Unit,
    onTripSearchClick: () -> Unit = {},
    onManageTrucks: () -> Unit,
    onManageClients: () -> Unit,
    onManageTruckOwners: () -> Unit,
    onQuickAdhocTruckClick: () -> Unit = {},
    startTab: ConnectTab? = null,
    startFilter: com.freight24.connect.feature.trips.domain.usecase.TripTypes? = null,
    onLogout: () -> Unit,
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    var selectedTab by rememberSaveable { mutableStateOf(startTab ?: ConnectTab.Home) }
    var selectedFilter by rememberSaveable { mutableStateOf(startFilter) }

    LaunchedEffect(Unit) {
        homeViewModel.registerDevice()
    }
    
    LaunchedEffect(startTab) {
        startTab?.let { selectedTab = it }
    }

    LaunchedEffect(startFilter) {
        startFilter?.let { selectedFilter = it }
    }

    BackHandler(enabled = selectedTab != ConnectTab.Home) {
        selectedTab = ConnectTab.Home
    }

    FreightBottomNavScaffold(
        items = ConnectTab.entries,
        selectedItem = selectedTab,
        onItemSelected = {
            if (it == ConnectTab.More) {
                navController.navigate(ConnectDestination.More.route)
            } else {
                selectedTab = it
            }
        },
        topBar = {
            AppTopBar(
                title = selectedTab.name,
                showSearch = selectedTab == ConnectTab.Home || selectedTab == ConnectTab.Trips,
                onSearchClick = when (selectedTab) {
                    ConnectTab.Home -> onSearchClick
                    ConnectTab.Trips -> onTripSearchClick
                    else -> null
                },
                showFilter = selectedTab == ConnectTab.Indents,
                onFilterClick = { /* TODO: Filter */ }
            )
        },
        floatingActionButton = {
            if (selectedTab == ConnectTab.Indents) {
                FloatingActionButton(
                    shape = CircleShape,
                    onClick = {
                        onCreateIndentClick()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.cd_add_indent),
                    )
                }
            }
        },
    ) { paddingValues, tab ->
        Crossfade(tab, label = "TabTransition") { currentTab ->
            when (currentTab) {
                ConnectTab.Home -> DashboardScreen(
                    paddingValues = paddingValues,
                    onCreateIndentClick = onCreateIndentClick,
                    onCreateTripClick = onQuickAdhocTruckClick,
                    onInterestInboxClick = { navController.navigate(ConnectDestination.InterestInbox.route) },
                    onVerificationRequestsClick = { tripCount, truckCount, truckOwnerCount, clientCount ->
                        navController.navigate(
                            ConnectDestination.VerificationRequests.createRoute(
                                tripCount, truckCount, truckOwnerCount, clientCount
                            )
                        )
                    },
                    onTripFilterClick = { filterType ->
                        navController.navigate(
                            ConnectDestination.FilteredTrips.createRoute(filterType.name)
                        )
                    },
                    onOpenIndentsClick = { selectedTab = ConnectTab.Indents },
                    onActiveAssignedClick = {
                        selectedFilter = TripTypes.ONGOING
                        selectedTab = ConnectTab.Trips
                    },
                    onActiveLoadingClick = {
                        selectedFilter = TripTypes.ONGOING
                        selectedTab = ConnectTab.Trips
                    },
                    onActiveInTransitClick = {
                        selectedFilter = TripTypes.IN_TRANSIT
                        selectedTab = ConnectTab.Trips
                    }
                )
                ConnectTab.Indents -> IndentsScreen(
                    onIndentClick = { indentId ->
                        onIndentClick(indentId)
                    },
                    onCloneClick = { indentId, indentGroupId ->
                        onCloneClick(indentId, indentGroupId)
                    },
                    onSearchClick = onSearchClick,
                    modifier = Modifier.padding(paddingValues)
                )
                ConnectTab.Trips -> TripsScreen(
                    paddingValues = paddingValues,
                    onTripClick = onTripClick,
                    initialFilter = selectedFilter
                )
                ConnectTab.Trucks -> FindTrucksScreen(
                    paddingValues = paddingValues,
                    onNavigateToSearch = { searchType ->
                        navController.navigate(ConnectDestination.SearchSelection.createRoute(searchType))
                    },
                    navController = navController
                )
                ConnectTab.More -> Spacer(Modifier.padding(paddingValues))
            }
        }
    }
}
