package com.freight24.trucker.feature.trucks.view

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.freight24.ui.UiState
import com.freight24.ui.components.FilterSelectionChip
import com.freight24.ui.components.TruckHeaderCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrucksScreen(
    paddingValues: PaddingValues,
    navController: NavController,
    viewModel: TrucksViewModel = hiltViewModel(),
    onTruckClick: (String) -> Unit = {},
    onOnboardClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchVisible by viewModel.isSearchVisible.collectAsState()
    val listState = rememberLazyListState()

    // Observe refresh signal from result of onboarding
    val refreshSignal =
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<Boolean>("refresh_trucks")

    LaunchedEffect(refreshSignal) {
        if (refreshSignal == true) {
            Log.d("TrucksScreen", "Refresh signal received, refreshing list")
            viewModel.onRefresh()
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("refresh_trucks")
            listState.scrollToItem(0)
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.onRefresh() },
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FilterChips(
                options = TruckFilter.entries,
                selected = selectedFilter,
                onSelected = viewModel::onFilterSelected,
                isSearchVisible = isSearchVisible,
                onSearchClick = viewModel::onSearchClick,
            )

            if (isSearchVisible) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search by Registration Number or Type") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search trucks by Registration Number or Type",
                            tint = colorScheme.onSurfaceVariant,
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outlineVariant,
                            focusedContainerColor = colorScheme.surface,
                            unfocusedContainerColor = colorScheme.surface
                        ),
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is UiState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp),
                            )
                            Button(onClick = { viewModel.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                    is UiState.Success -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            if (state.data.isEmpty()) {
                                item {
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(top = 100.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "No trucks found",
                                            style = MaterialTheme.typography.bodyLarge,
                                        )
                                    }
                                }
                            } else {
                                items(state.data) { truck ->
                                    TruckHeaderCard(
                                        registrationNumber = truck.registrationNumber,
                                        truckType = truck.truckType,
                                        availability = truck.availability,
                                        isVerified = truck.isVerified,
                                        isBlacklisted = truck.isBlacklisted,
                                        modifier = Modifier
                                            .semantics(mergeDescendants = true) { contentDescription = "truck_row_${truck.id}" }
                                            .clickable { onTruckClick(truck.id) },
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                OnboardTruckButton(onClick = onOnboardClick)
                                Spacer(modifier = Modifier.height(16.dp))
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
private fun OnboardTruckButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Onboard Your New Truck",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

@Composable
fun FilterChips(
    options: List<TruckFilter>,
    selected: TruckFilter,
    onSelected: (TruckFilter) -> Unit,
    isSearchVisible: Boolean,
    onSearchClick: () -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(options) { option ->
            FilterSelectionChip(
                label = option.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " "),
                isSelected = option == selected,
                onClick = { onSelected(option) },
                modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = "trucks_filter_${option.name.lowercase()}" },
            )
        }
        item {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = if (isSearchVisible) "Hide search" else "Search trucks",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}
