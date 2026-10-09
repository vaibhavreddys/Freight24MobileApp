package com.freight24.connect.feature.inbox.views

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.common.util.formatLoadWeight
import com.freight24.connect.feature.inbox.domain.viewmodel.InboxEvent
import com.freight24.connect.feature.inbox.domain.viewmodel.InboxViewModel
import com.freight24.connect.feature.inbox.domain.viewmodel.InboxViewModelImpl
import com.freight24.connect.feature.indents.views.ConnectIndentCard
import com.freight24.connect.feature.indents.views.docketTag
import com.freight24.connect.feature.indents.views.formatCurrencyOrNull
import com.freight24.rest.response.IndentInterestInboxResponse
import com.freight24.ui.components.FreightEmptyState
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.RefreshOnForeground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onBackClick: () -> Unit,
    viewModel: InboxViewModel = hiltViewModel<InboxViewModelImpl>(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    RefreshOnForeground { viewModel.onEvent(InboxEvent.SilentRefresh) }

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Interest Inbox", style = MaterialTheme.typography.titleMedium)
                        if (uiState.totalIndents > 0) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = colorScheme.errorContainer,
                                shape = CircleShape,
                                modifier = Modifier.size(24.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = uiState.totalIndents.toString(),
                                        color = colorScheme.error,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        containerColor = colorScheme.background,
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // Gated on emptiness so a silent foreground refresh cannot blank a
            // populated inbox; same shape as DashboardScreen's gate.
            if (uiState.isLoading && uiState.data.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.errorMessage != null && uiState.data.isEmpty()) {
                Text(
                    text = uiState.errorMessage!!,
                    color = colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                )
            } else if (uiState.data.isEmpty()) {
                FreightEmptyState(
                    icon = Icons.Outlined.Inbox,
                    title = "No interests yet",
                    subtitle = "When truck owners express interest in your indents, they will show up here.",
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.data) { indentData ->
                        IndentInterestCard(
                            indentData = indentData,
                            onRejectInterest = { interestId ->
                                viewModel.onEvent(InboxEvent.RejectInterest(interestId = interestId))
                            },
                        )
                    }
                }
            }
        }
    }
}

// ===========================================================================
// The inbox card is the indents page's docket, not a second card.
//
// It used to be a plain Material Card with hardcoded 16dp spacers and
// fontWeight literals — a different design system from the docket language the
// operator reads all day on the indents list. It now renders [ConnectIndentCard]
// directly and hangs the expandable trucker list off that card's footer slot, so
// the two screens cannot drift apart again.
//
// The dropdown behaviour is deliberately unchanged: a summary row that expands
// to the trucker list. What changed is that it now sits behind a perforation
// rule on a docket, and that rejecting asks first.
// ===========================================================================

@Composable
fun IndentInterestCard(
    indentData: IndentInterestInboxResponse.IndentInterestInboxData,
    onRejectInterest: (String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    var isExpanded by remember { mutableStateOf(false) }
    var truckerPendingReject by remember { mutableStateOf<IndentInterestInboxResponse.Trucker?>(null) }

    ConnectIndentCard(
        // Every row here is a live indent — the API filters to OPEN /
        // READY_FOR_ASSIGNMENT — so the spine takes the open accent without the
        // payload having to grow a status field it never carried.
        status = "OPEN",
        companyName = indentData.load.client.name,
        indentCode = indentData.load.indentCode,
        // The docket's stamp slot carries the reason this card is in the inbox:
        // how many truck owners are interested. The indents page uses that slot
        // for posted age, which this payload does not expose.
        postedTime = "${indentData.interestCount} interested",
        // Area over city, matching the indents page — "Nelamangala" tells an
        // operator more than "Bengaluru" when the payload carries both.
        fromCity =
            indentData.load.loading.area
                ?: indentData.load.loading.city
                ?: "Unknown",
        toCity =
            indentData.load.unloading.area
                ?: indentData.load.unloading.city
                ?: "Unknown",
        clientCostFormatted = formatCurrencyOrNull(indentData.load.pricing.clientCost),
        negotiatedCostFormatted = formatCurrencyOrNull(indentData.load.pricing.negotiatedCost),
        truckDisplay = indentData.load.truck.displayName,
        weightDisplay = formatLoadWeight(indentData.load.weight, "MT"),
        materialDisplay = indentData.load.material,
        onClick = { isExpanded = !isExpanded },
        footer = {
            Surface(
                onClick = { isExpanded = !isExpanded },
                color = Color.Transparent,
                modifier =
                    Modifier
                        .docketTag("interest_card_expand")
                        .semantics {
                            contentDescription =
                                if (isExpanded) {
                                    "Hide interested truck owners"
                                } else {
                                    "Show interested truck owners"
                                }
                        },
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.People,
                        contentDescription = null,
                        tint = scheme.primary,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = interestsLabel(indentData.interestCount),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector =
                            if (isExpanded) {
                                Icons.Default.KeyboardArrowUp
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    indentData.truckers.forEach { trucker ->
                        TruckerCallRow(
                            name = trucker.name,
                            phone = trucker.phone,
                            expressedAt = trucker.expressedAt,
                            onReject = { truckerPendingReject = trucker },
                        )
                    }
                }
            }
        },
    )

    // Rejecting withdraws someone else's interest and is not reversible from
    // the inbox, so it is confirmed rather than firing on a single tap.
    truckerPendingReject?.let { trucker ->
        AlertDialog(
            onDismissRequest = { truckerPendingReject = null },
            title = { Text("Reject this interest?") },
            text = {
                Text("${trucker.name} will be withdrawn from this indent.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRejectInterest(trucker.interestId)
                        truckerPendingReject = null
                    },
                    modifier = Modifier.docketTag("interest_reject_confirm"),
                ) {
                    Text("Reject", color = scheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { truckerPendingReject = null }) {
                    Text("Cancel")
                }
            },
            shape = MaterialTheme.shapes.large,
        )
    }
}

private fun interestsLabel(count: Int): String = "$count Interest${if (count == 1) "" else "s"} Expressed"
