package com.freight24.connect.feature.trucks.views
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.freight24.connect.feature.trucks.domain.viewmodel.ManageTrucksViewModel
import com.freight24.ui.UiState
import com.freight24.ui.components.FreightEmptyState
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.PanelShape
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTrucksScreen(
    navController: NavController,
    viewModel: ManageTrucksViewModel = hiltViewModel(),
    onAddTruckClick: () -> Unit,
    onTruckClick: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredTrucks by viewModel.filteredTrucks.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var isNavigatingBack by remember { mutableStateOf(false) }
    val navigationInteractionSource = remember { MutableInteractionSource() }

    // Observe refresh signal from result of onboarding
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow
            .collect { entry ->
                if (entry.savedStateHandle.get<Boolean>("refresh_trucks") == true) {
                    entry.savedStateHandle.remove<Boolean>("refresh_trucks")
                    viewModel.onRefresh()
                    listState.scrollToItem(0)
                }
            }
    }

    LaunchedEffect(listState, filteredTrucks.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastVisible ->
                if (lastVisible != null && lastVisible >= filteredTrucks.lastIndex - 3) {
                    viewModel.loadNextPage()
                }
            }
    }

    fun navigateBack() {
        if (isNavigatingBack) return
        isNavigatingBack = true
        onBackClick()
    }

    BackHandler(enabled = !isNavigatingBack) {
        navigateBack()
    }

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = {
                    Text(
                        text = "Trucks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (!isNavigatingBack) onAddTruckClick() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Onboard Truck",
                )
            }
        },
    ) { paddingValues ->
        Box(
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
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("trucks_search")
                            .semantics { testTagsAsResourceId = true },
                    placeholder = { Text("Search by registration number") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    shape = PanelShape,
                    singleLine = true,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                        ),
                )

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = viewModel::onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when (val state = uiState) {
                        is UiState.Loading, UiState.Idle -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is UiState.Error -> {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = PanelShape,
                                    color = MaterialTheme.colorScheme.errorContainer,
                                ) {
                                    Text(
                                        text = state.message,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                FilledTonalButton(onClick = viewModel::retry) {
                                    Text("Retry")
                                }
                            }
                        }
                        is UiState.Success -> {
                            if (filteredTrucks.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    FreightEmptyState(
                                        icon = Icons.Default.LocalShipping,
                                        title = if (searchQuery.isEmpty()) "No trucks yet" else "No matching trucks",
                                        subtitle =
                                            if (searchQuery.isEmpty()) {
                                                "Onboard your first truck to start assigning trips"
                                            } else {
                                                "Try a different registration number"
                                            },
                                    )
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    items(filteredTrucks, key = { it.truckId }) { truck ->
                                        TruckDocketCard(
                                            registrationNumber = truck.registrationNumber,
                                            truckType =
                                                listOfNotNull(
                                                    truck.truckType,
                                                    truck.vehicle?.capacityMt?.let { "${it.toString().removeSuffix(".0")} MT" },
                                                ).joinToString(" · ").ifBlank { null },
                                            availability = truck.availability,
                                            isVerified = truck.isVerified,
                                            isBlacklisted = truck.isBlacklisted,
                                            testTag = "trucks_card_${truck.registrationNumber}",
                                            onClick = { onTruckClick(truck.truckId) },
                                        )
                                    }
                                    if (isLoadingMore) {
                                        item {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                CircularProgressIndicator()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
            // Topmost scrim: once BACK is pressed this screen is animating out,
            // but its list items would still win hit-tests against the page
            // underneath (More). This overlay consumes those taps instead.
            if (isNavigatingBack) {
                Box(
                    modifier =
                        Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = navigationInteractionSource,
                                indication = null,
                                onClick = {},
                            ),
                )
            }
        }
    }
}
