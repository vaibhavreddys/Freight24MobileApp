package com.freight24.connect.feature.indents.views

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.common.util.DateTimeUtils.formatRelativeTime
import com.freight24.common.util.formatLoadWeight
import com.freight24.connect.R
import com.freight24.connect.feature.indents.domain.viewmodel.IndentStatusFilter
import com.freight24.connect.feature.indents.domain.viewmodel.IndentTab
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsListEvent
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsListUiState
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsListViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsListViewModelImpl
import com.freight24.rest.response.IndentResponse
import com.freight24.ui.components.FilterSelectionChip
import com.freight24.ui.components.FreightEmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndentsScreen(
    onIndentClick: (String) -> Unit = {},
    onCloneClick: (String, String) -> Unit = { s1, s2 -> },
    onSearchClick: () -> Unit = {},
    statusFilter: IndentStatusFilter = IndentStatusFilter.ACTIVE,
    onRefreshingChange: (Boolean) -> Unit = {},
    onPullFractionChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: IndentsListViewModel = hiltViewModel<IndentsListViewModelImpl>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(statusFilter) {
        viewModel.onEvent(IndentsListEvent.SelectStatusFilter(statusFilter))
    }

    // Pipe pull distance to HomeScreen
    LaunchedEffect(pullToRefreshState) {
        snapshotFlow { pullToRefreshState.distanceFraction }
            .collect { onPullFractionChange(it) }
    }

    // Pipe loading/refreshing state to HomeScreen
    LaunchedEffect(uiState.isLoading) {
        onRefreshingChange(uiState.isLoading)
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) { _ ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.onEvent(IndentsListEvent.RefreshIndents) },
            state = pullToRefreshState,
            indicator = {}, // <-- ADD: Hide default circular indicator
            modifier = Modifier.fillMaxSize(),
        ) {
            IndentsContent(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onIndentClick = onIndentClick,
                onCloneClick = onCloneClick,
                onSearchClick = onSearchClick,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun IndentsContent(
    uiState: IndentsListUiState,
    onEvent: (IndentsListEvent) -> Unit,
    onIndentClick: (String) -> Unit,
    onCloneClick: (String, String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab Switcher + (optional) search trigger share a single row so
            // the search sits inline to the right of the tabs instead of
            // pushing the list down. Search is only relevant on the ALL tab.
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IndentTabSwitcher(
                    selectedTab = uiState.selectedTab,
                    showReadyForAssignment = uiState.statusFilter == IndentStatusFilter.ACTIVE,
                    onTabSelected = { onEvent(IndentsListEvent.SelectTab(it)) },
                )
                if (uiState.selectedTab == IndentTab.ALL) {
                    Spacer(modifier = Modifier.width(12.dp))
                    SearchBarTrigger(
                        onClick = onSearchClick,
                    )
                }
            }

            if (uiState.isLoading && uiState.indents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorScheme.primary)
                }
            } else if (uiState.errorMessage != null && uiState.indents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.errorMessage, color = colorScheme.error)
                }
            } else {
                IndentsList(
                    indents = uiState.indents,
                    onIndentClick = onIndentClick,
                    onCloneClick = onCloneClick,
                    modifier = Modifier.fillMaxSize(),
                    showOwnership = uiState.selectedTab == IndentTab.ALL,
                )
            }
        }
    }
}

@Composable
fun SearchBarTrigger(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier =
            modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search indents",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun IndentTabSwitcher(
    selectedTab: IndentTab,
    showReadyForAssignment: Boolean,
    onTabSelected: (IndentTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterSelectionChip(
            label = "Mine",
            isSelected = selectedTab == IndentTab.MINE,
            onClick = { onTabSelected(IndentTab.MINE) },
            modifier =
                Modifier.docketTag("indents_filter_mine")
                    .semantics { selected = selectedTab == IndentTab.MINE },
        )
        if (showReadyForAssignment) {
            FilterSelectionChip(
                label = "Ready",
                isSelected = selectedTab == IndentTab.READY_FOR_ASSIGNMENT,
                onClick = { onTabSelected(IndentTab.READY_FOR_ASSIGNMENT) },
                modifier =
                    Modifier.docketTag("indents_filter_ready")
                        .semantics { selected = selectedTab == IndentTab.READY_FOR_ASSIGNMENT },
            )
        }
        FilterSelectionChip(
            label = "All",
            isSelected = selectedTab == IndentTab.ALL,
            onClick = { onTabSelected(IndentTab.ALL) },
            modifier =
                Modifier.docketTag("indents_filter_all")
                    .semantics { selected = selectedTab == IndentTab.ALL },
        )
    }
}

@Composable
fun IndentsList(
    indents: List<IndentResponse.Indent>,
    onIndentClick: (String) -> Unit,
    onCloneClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    showOwnership: Boolean = false,
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        if (indents.isEmpty()) {
            item(key = "indents_empty") {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    FreightEmptyState(
                        icon = Icons.Default.Description,
                        title = "No indents here yet",
                        subtitle = "Indents you create or are assigned will show up here",
                    )
                }
            }
        }
        items(indents) { indent ->
            val loadingLocation = indent.indentGroupDetails.indentLocations.find { it.locationType == "loading" }
            val unloadingLocation = indent.indentGroupDetails.indentLocations.find { it.locationType == "unloading" }
            val clientProfile = indent.indentGroupDetails.clientProfile
            val fleetExecutive = indent.fleetExecutive
            val ownership = indentCardOwnership(indent.indentOwnership)
            val showAdmin = ownership == IndentCardOwnership.OTHER

            ConnectIndentCard(
                companyName = clientProfile?.companyName ?: clientProfile?.fullName ?: "Unknown",
                indentCode = indent.indentCode,
                postedTime = formatRelativeTime(indent.createdAt),
                status = indent.status,
                fromCity = loadingLocation?.areaName ?: loadingLocation?.cityName ?: "Unknown",
                toCity = unloadingLocation?.areaName ?: unloadingLocation?.cityName ?: "Unknown",
                clientCostFormatted = formatCurrencyOrNull(indent.indentGroupDetails.clientTripCost),
                negotiatedCostFormatted = formatCurrencyOrNull(indent.negotiatedTripCost),
                truckDisplay = indent.indentGroupDetails.truckType?.displayName ?: "N/A",
                weightDisplay = formatLoadWeight(indent.indentGroupDetails.loadWeight, "MT"),
                materialDisplay = indent.indentGroupDetails.material.replaceFirstChar { it.uppercase() },
                interestCount =
                    if (
                        indent.canProgress ||
                        indent.canEdit ||
                        indent.indentOwnership == "CREATED_BY_ME"
                    ) {
                        indent.metrics?.interestCount
                    } else {
                        null
                    },
                showAdmin = showAdmin,
                adminName = fleetExecutive?.name,
                ownershipLabel = if (showOwnership) {
                    when (ownership) {
                        IndentCardOwnership.MINE -> stringResource(R.string.indent_ownership_mine)
                        IndentCardOwnership.OTHER -> stringResource(R.string.indent_ownership_other)
                        null -> null
                    }
                } else {
                    null
                },
                isMine = ownership == IndentCardOwnership.MINE,
                muted = showOwnership && ownership == IndentCardOwnership.MINE,
                onCallClick =
                    clientProfile?.phoneNumber?.takeIf { it.isNotBlank() }?.let { phone ->
                        {
                            val intent =
                                Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$phone")
                                }
                            context.startActivity(intent)
                        }
                    },
                onClick = { onIndentClick(indent.id) },
                onCloneClick = { onCloneClick(indent.id, indent.indentGroupDetails.id) },
            )
        }
    }
}

@Composable
fun RouteDot(isStart: Boolean) {
    Box(
        modifier =
            Modifier
                .size(12.dp)
                .background(
                    color = if (isStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    shape = CircleShape,
                ),
    )
}

@Composable
fun RouteVerticalLine() {
    Box(
        modifier =
            Modifier
                .padding(start = 3.dp) // center under 8dp dot
                .width(4.dp)
                .height(12.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
fun CostColumn(
    label: String,
    value: String,
    alignment: Alignment.Horizontal = Alignment.Start,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = alignment,
    ) {
        Text(
            text = label,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.5.sp,
                ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

fun formatCurrency(amount: Double?): String {
    if (amount == null) return "₹0"
    return "₹${"%,.0f".format(amount)}"
}

/**
 * [formatCurrency] for the places a missing rate should leave no trace at all
 * rather than print `₹0`. An unset rate and a zero rate both mean "not agreed
 * yet", and a card that advertises a rate of nothing is worse than one that
 * simply omits the line — the operator cannot tell the two apart on the docket.
 */
fun formatCurrencyOrNull(amount: Double?): String? =
    if (amount == null || amount <= 0.0) null else formatCurrency(amount)
