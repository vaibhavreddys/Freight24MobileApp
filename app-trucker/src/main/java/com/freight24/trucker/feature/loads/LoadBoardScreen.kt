package com.freight24.trucker.feature.loads

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.freight24.trucker.feature.loads.model.LoadBoardItem
import com.freight24.ui.components.CityCard
import com.freight24.ui.components.LoadCard

@Composable
fun LoadBoardScreen(
    paddingValues: PaddingValues,
    viewModel: LoadBoardViewModel,
    listState: LazyListState,
) {
    val items by viewModel.items.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val previousExpandedCityId = remember { mutableStateOf<Int?>(null) }

    val expandedCityId by viewModel.expandedCityId.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.bookmarkMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(expandedCityId, items) {
        val prev = previousExpandedCityId.value
        val current = expandedCityId

        // Only scroll when the expanded city CHANGES
        if (current != null && current != prev) {
            val index = viewModel.indexOfCity(current)
            if (index >= 0) {
                // Let LazyColumn settle after structural change
                kotlinx.coroutines.yield()

                listState.animateScrollToItem(index)
            }
        }

        previousExpandedCityId.value = current
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.onRefresh() },
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
    ) {
        LazyColumn(
            state = listState,
            modifier =
                Modifier
                    .fillMaxSize(),
            contentPadding =
                PaddingValues(
                    top = 12.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (items.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No loads available right now",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
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
                                onCallClick = {
                                    val phone = item.load.f24AgentPhone.trim()
                                    if (phone.isNotEmpty() && phone != "--") {
                                        val intent = Intent(Intent.ACTION_DIAL, "tel:$phone".toUri())
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onBookmarkClick = {
                                    viewModel.onBookmarkClicked(item.load.id)
                                },
                            )

                        is LoadBoardItem.ShowMoreItem -> {
                            TextButton(
                                onClick = { viewModel.onShowMoreClicked(item.cityId) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = "Show more loads",
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
