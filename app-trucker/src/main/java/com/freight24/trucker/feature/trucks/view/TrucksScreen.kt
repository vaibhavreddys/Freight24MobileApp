package com.freight24.trucker.feature.trucks.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.common.model.Truck
import com.freight24.ui.UiState
import com.freight24.ui.components.FilterSelectionChip

@Composable
internal fun TrucksScreen(
    paddingValues: PaddingValues,
    viewModel: TrucksViewModel = hiltViewModel(),
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
                                    TruckCard(truck)
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

@Composable
fun TruckCard(truck: Truck) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TruckIcon(truck.availability, truck.isBlacklisted)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = truck.registrationNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = truck.truckType ?: "Unknown Truck",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(truck.availability, truck.isBlacklisted)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            VerificationRow(truck)
        }
    }
}

@Composable
fun TruckIcon(status: String, isBlacklisted: Boolean) {
    val containerColor = when {
        isBlacklisted -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        status.uppercase() == "ACTIVE" || status.uppercase() == "AVAILABLE" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        status.uppercase() == "ON_TRIP" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(containerColor, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isBlacklisted) Icons.Default.Warning else Icons.Default.LocalShipping,
            contentDescription = null,
            tint = if (isBlacklisted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun StatusChip(status: String, isBlacklisted: Boolean) {
    val (text, color) = when {
        isBlacklisted -> "Blacklisted" to MaterialTheme.colorScheme.error
        status.uppercase() == "ACTIVE" || status.uppercase() == "AVAILABLE" -> "Available" to Color(0xFF2E7D32)
        status.uppercase() == "ON_TRIP" -> "On Trip" to Color(0xFF1976D2)
        else -> "Inactive" to MaterialTheme.colorScheme.outline
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun VerificationRow(truck: Truck) {
    val isVerified = truck.isVerified
    val (text, icon, color) = if (isVerified) {
        Triple("Fully Verified", Icons.Default.CheckCircle, Color(0xFF2E7D32))
    } else {
        Triple("Verification Pending", Icons.Default.Error, Color(0xFFF9A825))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
