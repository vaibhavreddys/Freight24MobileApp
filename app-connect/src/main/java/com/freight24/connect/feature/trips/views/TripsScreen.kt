package com.freight24.connect.feature.trips.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
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
import kotlinx.coroutines.flow.collect

@Composable
fun TripsScreen(
    paddingValues: PaddingValues,
    onTripClick: (String) -> Unit = {},
    initialFilter: TripTypes? = null,
    onRefreshingChange: (Boolean) -> Unit = {},
    onPullFractionChange: (Float) -> Unit = {},
    viewModel: TripsListViewModel = hiltViewModel<TripsListViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    val pullToRefreshState = rememberPullToRefreshState()

    androidx.compose.runtime.LaunchedEffect(initialFilter) {
        initialFilter?.let {
            viewModel.onEvent(TripsListEvent.SelectFilter(it))
        }
    }

    // Pipe pull distance to HomeScreen
    LaunchedEffect(pullToRefreshState) {
        snapshotFlow { pullToRefreshState.distanceFraction }
            .collect { onPullFractionChange(it) }
    }

    // Pipe loading/refreshing state to HomeScreen
    LaunchedEffect(uiState.isLoading) {
        onRefreshingChange(uiState.isLoading)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) { _ ->
    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.onEvent(TripsListEvent.Refresh) },
        state = pullToRefreshState,
        indicator = {},             // Hide default circular indicator
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
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
                            Text(text = uiState.errorMessage ?: "Unknown error", color = colorScheme.error)
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
                            Text(text = "No trips found", color = colorScheme.onSurfaceVariant)
                        }
                    }
                }

                else -> {
                    items(
                        items = uiState.trips,
                        key = { trip -> trip.id },
                    ) { trip ->
                        TripCard(
                            trip = trip,
                            onClick = { onTripClick(trip.id) },
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                    }
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
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
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
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }
    }
}
