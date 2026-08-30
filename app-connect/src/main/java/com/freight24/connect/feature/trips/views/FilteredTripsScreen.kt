package com.freight24.connect.feature.trips.views
import com.freight24.ui.components.FreightTopAppBar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.trips.domain.viewmodel.FilteredTripsViewModelImpl
import com.freight24.connect.feature.trips.domain.viewmodel.TripsListViewModel
import com.freight24.connect.feature.trips.views.components.TripCard
import com.freight24.connect.feature.trips.views.components.TripListItem
import com.freight24.ui.components.FreightEmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilteredTripsScreen(
    onBackClick: () -> Unit,
    onTripClick: (String) -> Unit,
    viewModel: TripsListViewModel = hiltViewModel<FilteredTripsViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = { 
                    Text(
                        text = uiState.selectedFilter.displayText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
            )
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (uiState.trips.isEmpty()) {
                FreightEmptyState(
                    icon = Icons.Outlined.LocalShipping,
                    title = "No trips found",
                    subtitle = "No trips match \"${uiState.selectedFilter.displayText}\" right now.",
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.trips) { trip ->
                        TripListItem(
                            trip = trip,
                            onClick = { onTripClick(trip.id) }
                        )
                    }
                }
            }
        }
    }
}
