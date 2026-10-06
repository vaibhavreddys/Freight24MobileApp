package com.freight24.trucker.feature.trucks.view

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.freight24.common.model.Truck
import com.freight24.ui.UiState
import com.freight24.ui.trucker.FleetCard
import com.freight24.ui.trucker.FleetEmptyState
import com.freight24.ui.trucker.FleetFilterEntry
import com.freight24.ui.trucker.FleetFilterRail
import com.freight24.ui.trucker.FleetSearchField
import com.freight24.ui.trucker.RigShape
import com.freight24.ui.trucker.TruckerColors
import com.freight24.ui.trucker.TruckerSecondaryButton
import com.freight24.ui.trucker.TruckerType
import com.freight24.ui.trucker.color
import com.freight24.ui.trucker.fleetPress
import com.freight24.ui.trucker.rememberTruckerColors
import com.freight24.ui.trucker.truckSidingLabel
import com.freight24.ui.trucker.truckSidingTone

// ---------------------------------------------------------------------------
// Trucks — the yard.
//
// Same language as Trips, same restraint: a siding down the left edge, the
// registration set in a plate, one hairline, one line of state. The two
// pages are meant to read as two views of one operation, so a driver
// moving between them should not have to relearn anything.
// ---------------------------------------------------------------------------

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
    val colors = rememberTruckerColors()

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
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(colors.canvas),
        ) {
            TruckFilterRow(
                selected = selectedFilter,
                isSearchVisible = isSearchVisible,
                colors = colors,
                onSelected = viewModel::onFilterSelected,
                onSearchClick = viewModel::onSearchClick,
            )

            if (isSearchVisible) {
                FleetSearchField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = "Search by plate or type",
                    colors = colors,
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator(
                            color = colors.brand,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    is UiState.Error -> {
                        Column(
                            modifier =
                                Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            FleetEmptyState(
                                title = "Could not load your fleet",
                                subtitle = state.message,
                                colors = colors,
                                shape = RigShape.OPEN,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TruckerSecondaryButton(
                                text = "Retry",
                                enabled = true,
                                colors = colors,
                                onClick = { viewModel.retry() },
                            )
                        }
                    }
                    is UiState.Success -> {
                        TruckList(
                            trucks = state.data,
                            listState = listState,
                            colors = colors,
                            onTruckClick = onTruckClick,
                            onOnboardClick = onOnboardClick,
                        )
                    }
                    UiState.Idle -> Unit
                }
            }
        }
    }
}

@Composable
private fun TruckFilterRow(
    selected: TruckFilter,
    isSearchVisible: Boolean,
    colors: TruckerColors,
    onSelected: (TruckFilter) -> Unit,
    onSearchClick: () -> Unit,
) {
    val filters = TruckFilter.entries
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FleetFilterRail(
            entries = filters.map { FleetFilterEntry(label = truckFilterLabel(it)) },
            selectedIndex = filters.indexOf(selected).coerceAtLeast(0),
            onSelect = { onSelected(filters[it]) },
            colors = colors,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onSearchClick)
                    .semantics { contentDescription = if (isSearchVisible) "Hide search" else "Search trucks" }
                    .testTag("trucks_search_btn"),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = if (isSearchVisible) colors.brand else colors.inkMuted,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

private fun truckFilterLabel(filter: TruckFilter): String =
    when (filter) {
        TruckFilter.ALL -> "All"
        TruckFilter.AVAILABLE -> "Available"
        TruckFilter.ON_TRIP -> "On trip"
        TruckFilter.BLACKLISTED -> "Blacklisted"
    }

@Composable
private fun TruckList(
    trucks: List<Truck>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    colors: TruckerColors,
    onTruckClick: (String) -> Unit,
    onOnboardClick: () -> Unit,
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(top = 2.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (trucks.isEmpty()) {
            item {
                FleetEmptyState(
                    title = "No trucks in the yard",
                    subtitle = "Onboard your first truck to start taking trips",
                    colors = colors,
                    shape = RigShape.TRAILER,
                )
            }
        } else {
            items(items = trucks, key = { it.id }) { truck ->
                RigCard(
                    truck = truck,
                    colors = colors,
                    onClick = { onTruckClick(truck.id) },
                    modifier =
                        Modifier.animateItem(
                            fadeInSpec = null,
                            placementSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                        ),
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                TruckerSecondaryButton(
                    text = "Onboard a truck",
                    enabled = true,
                    colors = colors,
                    icon = Icons.Filled.Add,
                    onClick = onOnboardClick,
                    testTag = "trucks_onboard_btn",
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Rig card
// ---------------------------------------------------------------------------

/**
 * One truck in the yard, built as a ledger row rather than a stack of badges.
 *
 * Four things are true of every truck — where it is parked, what it is, what
 * paperwork it has, and whether it is free — and only the first and last
 * change often. So the plate leads in type, the body and tonnage sit under it
 * as one muted line, and the two states share one right-hand column: colour
 * for parking, a plain word for paperwork. No boxes inside the card, so the
 * eye lands on the registration and stays there while scanning the column.
 */
@SuppressLint("RememberInComposition")
@Composable
internal fun RigCard(
    truck: Truck,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val interactionSource = MutableInteractionSource()
    val siding = truckSidingTone(truck.availability, truck.isBlacklisted)

    FleetCard(
        colors = colors,
        spine = siding.color(colors),
        modifier =
            modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fleetPress(interactionSource)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .testTag("truck_card")
                .semantics(mergeDescendants = true) {
                    testTagsAsResourceId = true
                    contentDescription = "truck_row_${truck.id}"
                },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // The plate, set as type instead of drawn as a badge: an
                // outlined box inside an outlined card put two borders around
                // every row, which is where the list started to read as
                // template rather than as a yard.
                Text(
                    text = truck.registrationNumber.uppercase(),
                    style = TruckerType.digit.copy(letterSpacing = 1.4.sp),
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // The truck type already names the body ("22 Feet Closed"),
                // so listing bodyType after it only ever read as a stutter.
                val spec =
                    listOfNotNull(
                        truck.truckType,
                        truck.capacityMt?.let { "${it.toString().removeSuffix(".0")} MT" },
                    ).joinToString(" · ")
                if (spec.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = spec,
                        style = TruckerType.caption,
                        color = colors.inkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Parking and paperwork, stacked and right-aligned so they form one
            // scannable column down the list.
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = truckSidingLabel(truck.availability, truck.isBlacklisted),
                    style = TruckerType.bodyStrong,
                    color = siding.color(colors),
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (truck.isVerified) "Verified" else "Not Verified",
                    style = TruckerType.caption,
                    color = colors.inkMuted,
                    maxLines = 1,
                )
            }
        }
    }
}
