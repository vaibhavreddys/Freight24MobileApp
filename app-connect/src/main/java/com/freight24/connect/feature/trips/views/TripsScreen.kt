package com.freight24.connect.feature.trips.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.trips.domain.usecase.TripTypes
import com.freight24.connect.feature.trips.domain.viewmodel.TripsListEvent
import com.freight24.connect.feature.trips.domain.viewmodel.TripsListViewModel
import com.freight24.connect.feature.trips.domain.viewmodel.TripsListViewModelImpl
import com.freight24.connect.feature.trips.views.components.TripCard

@Composable
fun TripsScreen(
    paddingValues: PaddingValues,
    onTripClick: (String) -> Unit = {},
    initialFilter: TripTypes? = null,
    viewModel: TripsListViewModel = hiltViewModel<TripsListViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(initialFilter) {
        initialFilter?.let {
            viewModel.onEvent(TripsListEvent.SelectFilter(it))
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.onEvent(TripsListEvent.Refresh) },
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(TripTypes.entries) { type ->
                        TripFilterChip(
                            text = type.displayText,
                            isSelected = uiState.selectedFilter == type,
                            onClick = { viewModel.onEvent(TripsListEvent.SelectFilter(type)) }
                        )
                    }
                }
            }

            when {
                uiState.isLoading && uiState.trips.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(top = 96.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                uiState.errorMessage != null && uiState.trips.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(top = 96.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = uiState.errorMessage ?: "Unknown error", color = Color.Red)
                        }
                    }
                }

                uiState.trips.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(top = 96.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No trips found", color = Color.Gray)
                        }
                    }
                }

                else -> {
                    items(uiState.trips) { trip ->
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
fun TripFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) Color(0xFF10B981) else Color.White,
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color.Gray,
                    fontSize = 12.sp
                )
            )
        }
    }
}
