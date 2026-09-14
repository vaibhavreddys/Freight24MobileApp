package com.freight24.connect.feature.leads.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.common.util.DateTimeUtils
import com.freight24.connect.feature.leads.domain.viewmodel.LeadsTab
import com.freight24.connect.feature.leads.domain.viewmodel.LeadsViewModel
import com.freight24.rest.response.LeadDto
import com.freight24.ui.components.DocketCard
import com.freight24.ui.components.FreightEmptyState
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.MetaTag
import com.freight24.ui.components.PanelShape
import com.freight24.ui.components.PerforationRule
import com.freight24.ui.components.TagShape
import com.freight24.ui.theme.FreightThemeTokens

// ===========================================================================
// Leads Management — "the lead tray".
//
// A tray of call-sheets next to the verification queue: a fixed four-up tally
// rail (every tab visible, every count rendered — even zero), then a stack of
// lead dockets. Same primitives as the rest of the desk: status spine, dog
// ear, perforated footer with a stamp for mixed-status tabs.
// ===========================================================================

/** Short rail labels — the rail is a fixed grid, so labels stay one word. */
private fun LeadsTab.railLabel(): String = when (this) {
    LeadsTab.NEW -> "New"
    LeadsTab.FOLLOW_UP -> "Follow-up"
    LeadsTab.COMPLETED -> "Onboarded"
    LeadsTab.NOT_INTERESTED -> "Closed"
}

/** Rail accent = the tab's dominant status colour, shared with the card spines. */
@Composable
private fun LeadsTabAccent(tab: LeadsTab): Color = when (tab) {
    LeadsTab.NEW -> FreightThemeTokens.colors.info
    LeadsTab.FOLLOW_UP -> FreightThemeTokens.colors.warning
    LeadsTab.COMPLETED -> FreightThemeTokens.colors.success
    LeadsTab.NOT_INTERESTED -> MaterialTheme.colorScheme.error
}

private fun LeadsTab.railIcon(): ImageVector = when (this) {
    LeadsTab.NEW -> Icons.Default.PersonAddAlt1
    LeadsTab.FOLLOW_UP -> Icons.Default.PhoneCallback
    LeadsTab.COMPLETED -> Icons.Default.HowToReg
    LeadsTab.NOT_INTERESTED -> Icons.Default.PersonOff
}

private fun LeadsTab.emptyTitle(): String = when (this) {
    LeadsTab.NEW -> "No new leads"
    LeadsTab.FOLLOW_UP -> "No follow-ups in progress"
    LeadsTab.COMPLETED -> "Nobody onboarded yet"
    LeadsTab.NOT_INTERESTED -> "No closed leads"
}

private fun LeadsTab.emptySubtitle(): String = when (this) {
    LeadsTab.NEW -> "Freshly imported leads wait here for their first call."
    LeadsTab.FOLLOW_UP -> "Leads you are actively working will appear here."
    LeadsTab.COMPLETED -> "Soft-onboarded and converted leads are filed here."
    LeadsTab.NOT_INTERESTED -> "Declined, invalid and do-not-contact leads are filed here."
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsScreen(
    onBackClick: () -> Unit,
    onLeadClick: (String) -> Unit,
    viewModel: LeadsViewModel = hiltViewModel(),
) {
    val leads = viewModel.leads.collectAsStateWithLifecycle().value
    val selectedTab = viewModel.selectedTab.collectAsStateWithLifecycle().value
    val isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value
    val isRefreshing = viewModel.isRefreshing.collectAsStateWithLifecycle().value
    val isLoadingMore = viewModel.isLoadingMore.collectAsStateWithLifecycle().value
    val hasMore = viewModel.hasMore.collectAsStateWithLifecycle().value
    val error = viewModel.error.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = {
                    Text(
                        text = "Leads Management",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LeadsQueueRail(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.setTab(it) },
                newCount = viewModel.countForTab(LeadsTab.NEW),
                followUpCount = viewModel.countForTab(LeadsTab.FOLLOW_UP),
                completedCount = viewModel.countForTab(LeadsTab.COMPLETED),
                closedCount = viewModel.countForTab(LeadsTab.NOT_INTERESTED),
            )

            if (error != null && leads.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = PanelShape,
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = "leads_list" },
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (leads.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        MetaTag(
                            text = "SHOWING ${selectedTab.railLabel().uppercase()}",
                            container = MaterialTheme.colorScheme.surfaceContainerHighest,
                            content = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FreightEmptyState(
                            icon = selectedTab.railIcon(),
                            title = selectedTab.emptyTitle(),
                            subtitle = selectedTab.emptySubtitle(),
                        )
                    }
                } else {
                    val listState = rememberLazyListState()
                    val shouldLoadMore = remember {
                        derivedStateOf {
                            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                                ?: return@derivedStateOf false
                            last.index >= listState.layoutInfo.totalItemsCount - 3
                        }
                    }
                    LaunchedEffect(shouldLoadMore.value) {
                        if (shouldLoadMore.value && hasMore) viewModel.loadMore()
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(leads, key = { it.id }) { lead ->
                            LeadDocketCard(
                                lead = lead,
                                // NEW is a single-status tab; the rail cell already
                                // says the state for every row, so the stamp would
                                // be noise. Mixed tabs need their per-card verdict.
                                showStamp = selectedTab != LeadsTab.NEW,
                                onClick = { onLeadClick(lead.id) },
                            )
                        }
                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===========================================================================
// Queue rail
// ===========================================================================

@Composable
private fun LeadsQueueRail(
    selectedTab: LeadsTab,
    onTabSelected: (LeadsTab) -> Unit,
    newCount: Int,
    followUpCount: Int,
    completedCount: Int,
    closedCount: Int,
) {
    val counts = mapOf(
        LeadsTab.NEW to newCount,
        LeadsTab.FOLLOW_UP to followUpCount,
        LeadsTab.COMPLETED to completedCount,
        LeadsTab.NOT_INTERESTED to closedCount,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            LeadsTab.entries.forEach { tab ->
                LeadsRailCell(
                    label = tab.railLabel(),
                    testTag = "leads_tab_${tab.name.lowercase()}",
                    icon = tab.railIcon(),
                    accent = LeadsTabAccent(tab),
                    count = counts[tab] ?: 0,
                    isSelected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
        )
    }
}

/**
 * One tally cell of the rail. Same geometry as the verification queue rail
 * (icon + tally on top, label below, equal widths — no hidden tabs); the
 * tally badge carries the tab's status accent so the workload can be read by
 * colour before it is read by text.
 */
@Composable
private fun LeadsRailCell(
    label: String,
    testTag: String,
    icon: ImageVector,
    accent: Color,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val container = if (isSelected) scheme.primaryContainer else Color.Transparent
    val content = if (isSelected) scheme.onPrimaryContainer else scheme.onSurfaceVariant
    val outline = if (isSelected) scheme.primary.copy(alpha = 0.32f) else scheme.outlineVariant.copy(alpha = 0.8f)
    val tallyContainer = if (isSelected) accent else accent.copy(alpha = 0.14f)
    val tallyContent = if (isSelected) scheme.onPrimary else accent

    Surface(
        modifier = modifier
            .heightIn(min = 56.dp)
            .testTag(testTag)
            .semantics { testTagsAsResourceId = true }
            .selectable(selected = isSelected, onClick = onClick, role = Role.Tab),
        shape = PanelShape,
        color = container,
        border = BorderStroke(1.dp, outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = content,
                )
                Spacer(Modifier.width(6.dp))
                Surface(
                    shape = TagShape,
                    color = tallyContainer,
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tallyContent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = content,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ===========================================================================
// Lead docket card
// ===========================================================================

@Composable
private fun LeadDocketCard(
    lead: LeadDto,
    showStamp: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val accent = LeadStatusAccent(lead.status)
    val cityLabel = lead.baseCity?.name ?: "No base city"
    val assignedLabel = lead.assignedUser?.fullName
        ?: lead.assignedUser?.tagCode
        ?: "Unassigned"

    DocketCard(
        accent = accent,
        testTag = "leads_card_${lead.phone}",
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = lead.name ?: "Unnamed lead",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(10.dp))
            AgeStamp(updatedAt = lead.updatedAt)
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = lead.phone,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = scheme.primary,
        )

        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = scheme.onSurfaceVariant,
            )
            Text(
                text = buildString {
                    append(cityLabel)
                    append(" · ${lead.callAttempts} attempt${if (lead.callAttempts == 1) "" else "s"}")
                    append(" · ${lead.truckTypes.size} truck${if (lead.truckTypes.size == 1) "" else "s"}")
                },
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.height(12.dp))
        PerforationRule()
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showStamp) {
                    LeadStatusStamp(status = lead.status)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = assignedLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Review",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = scheme.primary,
                )
            }
        }
    }
}

@Composable
private fun AgeStamp(updatedAt: String?) {
    val label = remember(updatedAt) { DateTimeUtils.formatRelativeTime(updatedAt) }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, TagShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
