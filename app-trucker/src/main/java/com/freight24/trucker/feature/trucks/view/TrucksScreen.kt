package com.freight24.trucker.feature.trucks.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.ui.UiState
import com.freight24.ui.components.FilterSelectionChip
import com.freight24.ui.components.TruckHeaderCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrucksScreen(
    paddingValues: PaddingValues,
    viewModel: TrucksViewModel = hiltViewModel(),
    onTruckClick: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.onRefresh() },
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FilterChips(
                options = TruckFilter.entries,
                selected = selectedFilter,
                onSelected = viewModel::onFilterSelected,
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is UiState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                            Button(onClick = { viewModel.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                    is UiState.Success -> {
                        if (state.data.isEmpty()) {
                            Text(
                                text = "No trucks found",
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(state.data) { truck ->
                                    TruckHeaderCard(
                                        registrationNumber = truck.registrationNumber,
                                        truckType = truck.truckType,
                                        availability = truck.availability,
                                        isVerified = truck.isVerified,
                                        isBlacklisted = truck.isBlacklisted,
                                        modifier = Modifier.clickable { onTruckClick(truck.id) }
                                    )
                                }
                            }
                        }
                    }
                    UiState.Idle -> {}
                }
            }
        }
    }
}

@Composable
fun FilterChips(
    options: List<TruckFilter>,
    selected: TruckFilter,
    onSelected: (TruckFilter) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(options) { option ->
            FilterSelectionChip(
                label = option.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " "),
                isSelected = option == selected,
                onClick = { onSelected(option) }
            )
        }
    }
}
