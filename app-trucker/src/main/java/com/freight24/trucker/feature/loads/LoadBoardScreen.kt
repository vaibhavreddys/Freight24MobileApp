package com.freight24.trucker.feature.loads

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freight24.core.ui.components.CityCard
import com.freight24.core.ui.components.LoadBoardTopBar
import com.freight24.core.ui.components.LoadCard
import com.freight24.trucker.feature.loads.model.LoadBoardItem

@Composable
fun LoadBoardScreen(
    paddingValues: PaddingValues,
    viewModel: LoadBoardViewModel,
    listState: LazyListState
) {
    val items by viewModel.items.collectAsState()
    val previousExpandedCityId = remember { mutableStateOf<String?>(null) }
    val expandedCityId by viewModel.expandedCityId.collectAsState()
    val animatedLoadIds = remember { mutableSetOf<String>() }


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



    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(
            top = 12.dp,
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = items,
            key = {
                when (it) {
                    is LoadBoardItem.CityItem -> "city-${it.cityId}"
                    is LoadBoardItem.LoadItem -> "load-${it.load.id}"
                    is LoadBoardItem.ShowMoreItem -> "show-more-${it.cityId}"
                }
            }
        ) { item ->
            when (item) {
                is LoadBoardItem.CityItem -> CityCard(
                    cityName = item.cityName,
                    listingCount = item.totalLoads,
                    expanded = item.expanded,
                    onClick = { viewModel.onCityClicked(item.cityId) }
                )

                is LoadBoardItem.LoadItem -> {
                    val loadId = item.load.id

                    val shouldAnimate = remember(loadId) {
                        !animatedLoadIds.contains(loadId)
                    }

                    if (shouldAnimate) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(
                                animationSpec = tween(
                                    durationMillis = 200
                                )
                            )
                        ) {
                            LoadCard(
                                sourceCity = item.load.sourceCity,
                                destinationCity = item.load.destinationCity,
                                postedTime = item.load.postedTime,
                                price = item.load.price,
                                truckInfo = item.load.truckInfo,
                                commodity = item.load.commodity,
                                loadWeight = item.load.loadWeight,
                                f24AgentName = item.load.f24AgentName,
                                onCallClick = {},
                                onBookmarkClick = {}
                            )
                        }

                        // Mark as animated AFTER first render
                        LaunchedEffect(Unit) {
                            animatedLoadIds.add(loadId)
                        }
                    } else {
                        // Already seen → render normally (no animation)
                        LoadCard(
                            sourceCity = item.load.sourceCity,
                            destinationCity = item.load.destinationCity,
                            postedTime = item.load.postedTime,
                            price = item.load.price,
                            truckInfo = item.load.truckInfo,
                            commodity = item.load.commodity,
                            loadWeight = item.load.loadWeight,
                            f24AgentName = item.load.f24AgentName,
                            onCallClick = {},
                            onBookmarkClick = {}
                        )
                    }
                }



                is LoadBoardItem.ShowMoreItem -> {
                    TextButton(
                        onClick = { viewModel.onShowMoreClicked(item.cityId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Show more loads",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
