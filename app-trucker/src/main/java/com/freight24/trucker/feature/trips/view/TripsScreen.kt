package com.freight24.trucker.feature.trips.view

import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.foundation.layout.Arrangement
import com.freight24.ui.components.TripCard
import com.freight24.ui.trucker.FleetEmptyState
import com.freight24.ui.trucker.FleetFilterEntry
import com.freight24.ui.trucker.FleetFilterRail
import com.freight24.ui.trucker.RigShape
import com.freight24.ui.trucker.TruckerColors
import com.freight24.ui.trucker.rememberTruckerColors

// ---------------------------------------------------------------------------
// Trips — the running manifest.
//
// One screen, one job: show every trip the driver is on and let them get to
// the detail. The filter rail sits in a single bordered strip (a manifest
// tab, not a scatter of chips) and the list below is a column of
// consignment slips. Nothing else competes for attention.
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onTripClick: (String) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
) {
    val viewModel: TripsViewModel = hiltViewModel()
    val trips by viewModel.filteredTrips.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val filterCounts by viewModel.filterCounts.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isInitialLoading by viewModel.isInitialLoading.collectAsState()

    val colors = rememberTruckerColors()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.onRefresh() },
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(colors.canvas),
        ) {
            TripFilterRail(
                selectedFilter = selectedFilter,
                filterCounts = filterCounts,
                colors = colors,
                onFilterSelected = { viewModel.onFilterSelected(it) },
            )

            if (trips.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isInitialLoading) {
                        CircularProgressIndicator(color = colors.brand)
                    } else {
                        FleetEmptyState(
                            title = emptyTitleFor(selectedFilter),
                            subtitle = "Trips assigned to you will show up here",
                            colors = colors,
                            shape = RigShape.TRAILER,
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 2.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    items(
                        items = trips,
                        key = { it.id },
                    ) { trip ->
                        TripCard(
                            trip = trip,
                            colors = colors,
                            onClick = { onTripClick(trip.id) },
                            modifier = Modifier.animateItem(
                                fadeInSpec = null,
                                placementSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                            ),
                        )
                    }
                }
            }
        }
    }
}

private fun emptyTitleFor(filter: TripFilter): String =
    when (filter) {
        TripFilter.ACTIVE -> "No trips on the road"
        TripFilter.POD -> "Nothing waiting on POD"
        TripFilter.COMPLETED -> "No settled trips yet"
        TripFilter.ALL -> "No trips yet"
    }

@Composable
private fun TripFilterRail(
    selectedFilter: TripFilter,
    filterCounts: Map<TripFilter, Int>,
    colors: TruckerColors,
    onFilterSelected: (TripFilter) -> Unit,
) {
    val filters = TripFilter.entries
    FleetFilterRail(
        entries =
            filters.map { filter ->
                FleetFilterEntry(
                    label = filter.name.lowercase().replaceFirstChar { it.uppercase() },
                    count = filterCounts[filter]?.takeIf { filter == TripFilter.ACTIVE || filter == TripFilter.POD },
                )
            },
        selectedIndex = filters.indexOf(selectedFilter).coerceAtLeast(0),
        onSelect = { onFilterSelected(filters[it]) },
        colors = colors,
    )
}
