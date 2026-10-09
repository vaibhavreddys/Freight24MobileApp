package com.freight24.trucker.feature.loads

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.freight24.trucker.feature.loads.model.LoadBoardItem
import com.freight24.ui.components.CityCard
import com.freight24.ui.components.FreightEmptyState
import com.freight24.ui.components.LoadCard
import com.freight24.ui.components.LoadDetailBottomSheet
import com.freight24.ui.components.RefreshOnForeground
import com.freight24.ui.theme.FreightThemeTokens
import com.freight24.ui.trucker.FleetSearchField
import com.freight24.ui.trucker.rememberTruckerColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadBoardScreen(
    paddingValues: PaddingValues,
    viewModel: LoadBoardViewModel,
    listState: LazyListState,
) {
    val items by viewModel.items.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isInitialLoading by viewModel.isInitialLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchVisible by viewModel.isSearchVisible.collectAsState()
    val previousExpandedCityId = remember { mutableStateOf<Int?>(null) }
    val truckerColors = rememberTruckerColors()

    val expandedCityId by viewModel.expandedCityId.collectAsState()
    val context = LocalContext.current

    val selectedIndentId by viewModel.selectedIndentId.collectAsState()
    val indentDetailState by viewModel.indentDetailState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.bookmarkMessage.collect { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.detailMessage.collect { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    // onRefresh() drives _isRefreshing, not _isInitialLoading, so the board
    // stays on screen and the spinner that replaces an empty board never appears.
    RefreshOnForeground { viewModel.onRefresh() }

    LaunchedEffect(expandedCityId, items) {
        val prev = previousExpandedCityId.value
        val current = expandedCityId

        // Only scroll when the expanded city CHANGES
        if (current != null && current != prev) {
            // Land on the first child of the new city, not the city header,
            // so the user sees the loads immediately and the previous city's
            // disappear-animation (animateItem) is pushed above the viewport
            // instead of being frozen under the new header.
            val targetIndex = viewModel.indexOfCity(current)
            if (targetIndex >= 0) {
                kotlinx.coroutines.yield()
                // +1 because the items list always places the new city
                // header before its own load rows.
                val firstChildIndex = targetIndex + 1
                if (firstChildIndex < items.size) {
                    listState.animateScrollToItem(firstChildIndex)
                } else {
                    listState.animateScrollToItem(targetIndex)
                }
            }
        } else if (current == null && prev != null) {
            // A city collapsed. Its indent rows are gone, so a scroll offset
            // parked inside them now points past the end of a much shorter
            // list and the user would be dropped at the bottom staring at
            // unrelated cities. Bring the header they just collapsed back into
            // view instead: it is the visible confirmation that back worked,
            // and it leaves them parked where a second back press exits from.
            val collapsedIndex = viewModel.indexOfCity(prev)
            if (collapsedIndex >= 0) {
                kotlinx.coroutines.yield()
                listState.animateScrollToItem(collapsedIndex)
            }
        }

        previousExpandedCityId.value = current
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isSearchVisible) {
                FleetSearchField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = "Search cities",
                    colors = truckerColors,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.onRefresh() },
                modifier = Modifier.weight(1f),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            top = 16.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 24.dp,
                        ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (items.isEmpty()) {
                        item(key = "empty") {
                            Box(
                                modifier =
                                    Modifier
                                        .fillParentMaxSize()
                                        .semantics(mergeDescendants = true) { contentDescription = "loads_empty_state" },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isInitialLoading) {
                                    CircularProgressIndicator()
                                } else {
                                    if (searchQuery.isNotBlank()) {
                                        FreightEmptyState(
                                            icon = Icons.Default.LocalShipping,
                                            title = "No matching cities",
                                            subtitle = "Try another city name",
                                        )
                                    } else {
                                        FreightEmptyState(
                                            icon = Icons.Default.LocalShipping,
                                            title = "No loads available right now",
                                            subtitle = "Pull to refresh — new loads appear here the moment agents post them",
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Board status strip — dominant secondary region answering
                        // "what's live?" before the city list (Purpose First +
                        // System Status Visibility). Tonal surfaceContainerLow keeps
                        // it subordinate to the load cards themselves.
                        item(key = "board_header") {
                            val cityCount = items.filterIsInstance<LoadBoardItem.CityItem>().size
                            val totalLoads = items.filterIsInstance<LoadBoardItem.CityItem>().sumOf { it.totalLoads }
                            val extended = FreightThemeTokens.colors
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                ) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(8.dp)
                                                .background(color = extended.success, shape = CircleShape),
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "LIVE BOARD • $cityCount ${if (cityCount == 1) "city" else "cities"} • $totalLoads ${if (totalLoads == 1) "load" else "loads"}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = "Tap a city to see its loads",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                        items(
                            items = items,
                            key = {
                                when (it) {
                                    is LoadBoardItem.CityItem -> "city-${it.cityId}-${it.cityName}"
                                    is LoadBoardItem.LoadItem -> "load-${it.cityId}-${it.load.id}"
                                    is LoadBoardItem.ShowMoreItem -> "show-more-${it.cityId}"
                                }
                            },
                            contentType = {
                                when (it) {
                                    is LoadBoardItem.CityItem -> "city"
                                    is LoadBoardItem.LoadItem -> "load"
                                    is LoadBoardItem.ShowMoreItem -> "show_more"
                                }
                            },
                        ) { item ->
                            when (item) {
                                is LoadBoardItem.CityItem ->
                                    CityCard(
                                        cityName = item.cityName,
                                        listingCount = item.totalLoads,
                                        expanded = item.expanded,
                                        onClick = { viewModel.onCityClicked(item.cityId) },
                                        modifier =
                                            Modifier
                                                // No placement animation on the city
                                                // card itself: the previous-expanded
                                                // card kept its body rendered under
                                                // the new expanded card for the
                                                // duration of the tween, and the
                                                // tween left a "ghost" header from
                                                // the next city stacked under the
                                                // collapsed previous one. Letting
                                                // the LazyColumn re-layout the
                                                // header instantly removes the
                                                // artefact.
                                                //
                                                // mergeDescendants is set on the
                                                // inner Card by CityCard so the
                                                // whole row is one tap target; no
                                                // extra contentDescription is added
                                                // here because cityId is encoded in
                                                // the child Text content and a
                                                // duplicate semantics node was being
                                                // emitted during the collapse /
                                                // expand transition.
                                                .testTag("city_card")
                                                .semantics {
                                                    testTagsAsResourceId = true
                                                },
                                    )

                                is LoadBoardItem.LoadItem ->
                                    LoadCard(
                                        sourceCity = item.load.sourceCity,
                                        destinationCity = item.load.destinationCity,
                                        postedTime = item.load.postedTime,
                                        price = item.load.price,
                                        truckInfo = item.load.truckInfo,
                                        commodity = item.load.commodity,
                                        loadWeight = item.load.loadWeight,
                                        f24AgentName = item.load.f24AgentName,
                                        isBookmarked = item.load.isInterested,
                                        isBookmarkLoading = item.load.isInterestLoading,
                                        modifier =
                                            Modifier
                                                // No animateItem on the load rows:
                                                // when a previous expanded city
                                                // collapses and a new one expands in
                                                // the same frame, animateItem kept
                                                // the disappearing rows rendered
                                                // under the new header for the
                                                // duration of the tween — visible as
                                                // a "ghost" of the previous city's
                                                // loads. Without animateItem the
                                                // LazyColumn settles to the new
                                                // layout immediately.
                                                //
                                                // mergeDescendants / contentDescription
                                                // removed here for the same reason as
                                                // the city card above: a
                                                // mergeDescendants wrapper was leaving
                                                // a stale semantic node across the
                                                // collapse/expand transition.
                                                .testTag("load_card")
                                                .semantics {
                                                    testTagsAsResourceId = true
                                                },
                                        onClick = {
                                            viewModel.onLoadCardClicked(
                                                indentId = item.load.id,
                                                tripPrice = item.load.price,
                                            )
                                        },
                                        onCallClick = {
                                            val phone = item.load.f24AgentPhone.trim()
                                            if (phone.isNotEmpty() && phone != "--") {
                                                val intent = Intent(Intent.ACTION_DIAL, "tel:$phone".toUri())
                                                context.startActivity(intent)
                                            }
                                        },
                                        onBookmarkClick = {
                                            viewModel.onBookmarkClicked(item.load.id)
                                        },
                                    )

                                is LoadBoardItem.ShowMoreItem -> {
                                    TextButton(
                                        onClick = { viewModel.onShowMoreClicked(item.cityId) },
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .testTag("show_more_btn")
                                                .semantics { testTagsAsResourceId = true },
                                    ) {
                                        Text(
                                            text = "Show more (${item.loadedCount} of ${item.totalCount})",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
        )
    }

    if (selectedIndentId != null) {
        LoadDetailBottomSheet(
            state = indentDetailState,
            onDismiss = { viewModel.onDismissIndentSheet() },
            onCallAgent = { phone ->
                val trimmed = phone.trim()
                if (trimmed.isNotEmpty() && trimmed != "--") {
                    val intent = Intent(Intent.ACTION_DIAL, "tel:$trimmed".toUri())
                    context.startActivity(intent)
                }
            },
            onToggleInterest = { viewModel.onToggleInterestInSheet() },
        )
    }
}
