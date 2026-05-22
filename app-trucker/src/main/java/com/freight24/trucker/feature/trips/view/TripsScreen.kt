package com.freight24.trucker.feature.trips.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.ui.components.FilterSelectionChip
import com.freight24.ui.components.TripCard

@Preview
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
                    .background(MaterialTheme.colorScheme.background),
        ) {
            // Filter Tabs
            TripFilters(
                selectedFilter = selectedFilter,
                filterCounts = filterCounts,
                onFilterSelected = { viewModel.onFilterSelected(it) },
            )

            // Trips List or Empty State
            if (trips.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No ${selectedFilter.name.lowercase().replaceFirstChar { it.uppercase() }} Trips Available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    items(
                        items = trips,
                        key = { it.id },
                    ) { trip ->
                        TripCard(
                            trip = trip,
                            onClick = { onTripClick(trip.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TripFilters(
    selectedFilter: TripFilter,
    filterCounts: Map<TripFilter, Int>,
    onFilterSelected: (TripFilter) -> Unit,
) {
    LazyRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(TripFilter.entries.toTypedArray()) { filter ->
            val count = if (filter == TripFilter.ACTIVE || filter == TripFilter.POD) {
                filterCounts[filter]
            } else {
                null
            }

            FilterSelectionChip(
                label = filter.name,
                isSelected = selectedFilter == filter,
                count = count,
                onClick = { onFilterSelected(filter) },
            )
        }
    }
}
