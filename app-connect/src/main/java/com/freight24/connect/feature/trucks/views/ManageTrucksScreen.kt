package com.freight24.connect.feature.trucks.views
import com.freight24.ui.components.FreightTopAppBar

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.freight24.connect.feature.trucks.domain.viewmodel.ManageTrucksViewModel
import com.freight24.ui.UiState
import com.freight24.ui.components.TruckHeaderCard
import com.freight24.ui.theme.FreightThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTrucksScreen(
    navController: NavController,
    viewModel: ManageTrucksViewModel = hiltViewModel(),
    onAddTruckClick: () -> Unit,
    onTruckClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredTrucks by viewModel.filteredTrucks.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val listState = rememberLazyListState()

    // Observe refresh signal from result of onboarding
    val refreshSignal =
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<Boolean>("refresh_trucks")

    LaunchedEffect(refreshSignal) {
        if (refreshSignal == true) {
            Log.d("ManageTrucksScreen", "Refresh signal received, refreshing list")
            viewModel.onRefresh()
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("refresh_trucks")
            listState.scrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                FreightTopAppBar(
                    title = {
                        Text(
                            text = "Trucks",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTruckClick,
                modifier = Modifier
                    .padding(end = 4.dp, bottom = 4.dp),
                containerColor = FreightThemeTokens.colors.success,
                contentColor = FreightThemeTokens.colors.onSuccess,
                shape = RoundedCornerShape(16.dp),
            ) {
//                Icon(Icons.Default.Add, contentDescription = "Onboard Truck")
                Box(
                    modifier = Modifier.size(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = com.freight24.connect.R.drawable.ic_add_truck),
                        contentDescription = "Onboard Truck"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by Registration Number") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = viewModel::onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = viewModel::retry) {
                            Text("Retry")
                        }
                    }
                }
                is UiState.Success -> {
                    if (filteredTrucks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = if (searchQuery.isEmpty()) "No trucks available" else "No matching trucks found")
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(end = 16.dp, bottom = 16.dp, start = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredTrucks, key = { it.truckId }) { truck ->
                                TruckHeaderCard(
                                    registrationNumber = truck.registrationNumber,
                                    truckType = truck.truckType ?: "Unknown Type",
                                    availability = truck.availability,
                                    isVerified = truck.isVerified,
                                    isBlacklisted = truck.isBlacklisted,
                                    modifier = Modifier.clickable { 
                                        onTruckClick(truck.truckId)
                                    }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
            }
        }
    }
}
