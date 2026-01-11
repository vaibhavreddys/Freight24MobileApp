package com.freight24.trucker.feature.trips

//import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.freight24.core.ui.components.TripCard

@Preview
@Composable
fun TripsScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    viewModel: TripsViewModel = hiltViewModel(),
    onTripClick: (String) -> Unit = {}
) {
    val trips by viewModel.filteredTrips.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.onRefresh() },
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter Tabs
            TripFilterTabs(
                selectedFilter = selectedFilter,
                onFilterSelected = viewModel::onFilterSelected
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Trips List
            if (trips.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No trips found", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(trips) { trip ->
                        TripCard(
                            trip = trip,
                            onClick = { onTripClick(trip.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TripFilterTabs(
    selectedFilter: TripFilter,
    onFilterSelected: (TripFilter) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FilterTab(
                label = "Active",
                isSelected = selectedFilter == TripFilter.ACTIVE,
                onClick = { onFilterSelected(TripFilter.ACTIVE) }
            )
        }
        item {
            FilterTab(
                label = "POD",
                isSelected = selectedFilter == TripFilter.POD,
                onClick = { onFilterSelected(TripFilter.POD) }
            )
        }
        item {
            FilterTab(
                label = "Completed",
                isSelected = selectedFilter == TripFilter.COMPLETED,
                onClick = { onFilterSelected(TripFilter.COMPLETED) }
            )
        }
        item {
            FilterTab(
                label = "All",
                isSelected = selectedFilter == TripFilter.ALL,
                onClick = { onFilterSelected(TripFilter.ALL) }
            )
        }
    }
}

@Composable
fun FilterTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF1B1B3A) else Color.White
    val textColor = if (isSelected) Color.White else Color.Gray
    val borderColor = if (isSelected) Color.Transparent else Color(0xFFEEEEEE)

    Surface(
        modifier = Modifier.clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}
