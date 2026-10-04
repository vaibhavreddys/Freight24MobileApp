package com.freight24.connect.feature.dashboard.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.sin
import kotlinx.coroutines.isActive
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.freight24.connect.feature.dashboard.domain.viewmodel.DashboardEvent
import com.freight24.connect.feature.dashboard.domain.viewmodel.DashboardViewModel
import com.freight24.connect.feature.dashboard.domain.viewmodel.DashboardViewModelImpl
import com.freight24.connect.feature.trips.domain.usecase.TripTypes
import com.freight24.rest.response.DashboardDataResponse
import com.freight24.ui.components.BoundaryBadge
import com.freight24.ui.components.BoundaryBadgeTone
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

@Composable
fun DashboardScreen(
    paddingValues: PaddingValues,
    viewModel: DashboardViewModel = hiltViewModel<DashboardViewModelImpl>(),
    onCreateIndentClick: () -> Unit = {},
    onCreateTripClick: () -> Unit = {},
    onInterestInboxClick: () -> Unit = {},
    onVerificationRequestsClick: (Int, Int, Int, Int) -> Unit = { _, _, _, _ -> },
    onTripFilterClick: (TripTypes) -> Unit = {},
    onOpenIndentsClick: () -> Unit = {},
    onActiveAssignedClick: () -> Unit = {},
    onActiveLoadingClick: () -> Unit = {},
    onActiveInTransitClick: () -> Unit = {},
    onRefresh: () -> Unit = { viewModel.onEvent(DashboardEvent.Refresh) },
    onRefreshingChange: (Boolean) -> Unit = {},
    onPullFractionChange: (Float) -> Unit = {},
) {
    val uiState = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()
    val pullToRefreshState = rememberPullToRefreshState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(pullToRefreshState) {
        snapshotFlow { pullToRefreshState.distanceFraction }
            .collect { onPullFractionChange(it) }
    }

    LaunchedEffect(uiState.isRefreshing) {
        onRefreshingChange(uiState.isRefreshing)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(DashboardEvent.Refresh)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        state = pullToRefreshState,
        indicator = {},
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(colorScheme.background)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.isLoading && uiState.data == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null && uiState.data == null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(uiState.error, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.onEvent(DashboardEvent.Retry) }) {
                        Text("Retry")
                    }
                }
            } else {
                uiState.data?.let { data ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        AttentionNeededSection(
                            attentionNeeded = data.attentionNeeded,
                            onTripFilterClick = onTripFilterClick,
                            onInterestInboxClick = onInterestInboxClick
                        )
                        IndentPulseSection(
                            indentPulse = data.indentPulse,
                            onOpenClick = onOpenIndentsClick
                        )
                        ActiveTripsSection(
                            activeTrips = data.activeTrips,
                            onAssignedClick = onActiveAssignedClick,
                            onLoadingClick = onActiveLoadingClick,
                            onInTransitClick = onActiveInTransitClick
                        )
                        PendingActionsSection(
                            pendingActions = data.pendingActions,
                            onTripFilterClick = onTripFilterClick
                        )
                        QuickActionsSection(
                            attentionNeeded = data.attentionNeeded,
                            pendingActions = data.pendingActions,
                            onCreateIndentClick = onCreateIndentClick,
                            onCreateTripClick = onCreateTripClick,
                            onInterestInboxClick = onInterestInboxClick,
                            onVerificationRequestsClick = onVerificationRequestsClick,
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AttentionNeededSection(
    attentionNeeded: DashboardDataResponse.AttentionNeeded,
    onTripFilterClick: (TripTypes) -> Unit,
    onInterestInboxClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column {
        SectionHeader("ATTENTION NEEDED")
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Trucker Interview Card
            AttentionCard(
                modifier = Modifier.weight(1f),
                title = "Trucker\nInterests",
                value = "${attentionNeeded.truckerInterests} New Leads",
                backgroundColor = colorScheme.primaryContainer,
                valueColor = colorScheme.primary,
                onClick = onInterestInboxClick
            )
            // Waiting for LR Card
            AttentionCard(
                modifier = Modifier.weight(1f),
                title = "LR upload pending",
                value = "${attentionNeeded.waitingForLR} Pending",
                backgroundColor = colorScheme.tertiaryContainer,
                valueColor = colorScheme.tertiary,
                onClick = { onTripFilterClick(TripTypes.LR_NOT_UPLOADED) }
            )
//            // Payment Enquiry (Mocked for UI)
//            AttentionCard(
//                modifier = Modifier.weight(1f),
//                title = "Payment\nEnquiry",
//                value = "1 Urgent",
//                backgroundColor = colorScheme.errorContainer,
//                valueColor = colorScheme.error
//            )
        }
    }
}

@Composable
fun AttentionCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    backgroundColor: Color,
    valueColor: Color,
    onClick: () -> Unit = {}
) {

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
fun IndentPulseSection(
    indentPulse: DashboardDataResponse.IndentPulse,
    onOpenClick: () -> Unit = {}
) {
    Column {
        SectionHeader("INDENT PULSE")
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PulseCard(modifier = Modifier.weight(1f), title = "TOTAL", count = indentPulse.total.toString(), color = MaterialTheme.colorScheme.onSurface)
            PulseCard(modifier = Modifier.weight(1f), title = "CLOSED", count = indentPulse.closed.toString(), color = MaterialTheme.colorScheme.primary)
            PulseCard(modifier = Modifier.weight(1f), title = "OPEN", count = indentPulse.open.toString(), color = MaterialTheme.colorScheme.tertiary, onClick = onOpenClick)
        }
    }
}

@Composable
fun PulseCard(
    modifier: Modifier = Modifier,
    title: String,
    count: String,
    color: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                count,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun ActiveTripsSection(
    activeTrips: DashboardDataResponse.ActiveTrips,
    onAssignedClick: () -> Unit = {},
    onLoadingClick: () -> Unit = {},
    onInTransitClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Active Trips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${activeTrips.total} Total",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress Bars
            val safeTotal = if (activeTrips.total > 0) activeTrips.total.toFloat() else 1f
            
            TripProgressRow("ASSIGNED", "${activeTrips.assigned} TRIP", activeTrips.assigned / safeTotal, MaterialTheme.colorScheme.primary, onAssignedClick)
            TripProgressRow("LOADING", "${activeTrips.loading} TRIP", activeTrips.loading / safeTotal, MaterialTheme.colorScheme.tertiary, onLoadingClick)
            TripProgressRow("IN-TRANSIT", "${activeTrips.inTransit} TRIPS", activeTrips.inTransit / safeTotal, MaterialTheme.colorScheme.secondary, onInTransitClick)
        }
    }
}

@Composable
fun TripProgressRow(
    label: String,
    countText: String,
    progress: Float,
    color: Color,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                countText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
fun PendingActionsSection(
    pendingActions: DashboardDataResponse.PendingActions,
    onTripFilterClick: (com.freight24.connect.feature.trips.domain.usecase.TripTypes) -> Unit
) {
    Column {
        SectionHeader("PENDING ACTIONS")
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionCard(
                icon = Icons.Default.Description,
                iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                iconTintColor = MaterialTheme.colorScheme.primary,
                title = "LR waiting for review",
                subtitle = "${pendingActions.lrReviews} documents pending",
                onClick = { onTripFilterClick(TripTypes.LR_PENDING_REVIEW) }
            )
            ActionCard(
                icon = Icons.Default.VerticalSplit, // A box icon placeholder
                iconBackgroundColor = MaterialTheme.colorScheme.errorContainer,
                iconTintColor = MaterialTheme.colorScheme.error,
                title = "POD Follow-up",
                subtitle = "${pendingActions.podFollowUp} action required",
                onClick = { onTripFilterClick(TripTypes.WAITING_FOR_POD) }
            )
        }
    }
}

@Composable
fun ActionCard(
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTintColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = iconBackgroundColor,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTintColor)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun QuickActionsSection(
    attentionNeeded: DashboardDataResponse.AttentionNeeded,
    pendingActions: DashboardDataResponse.PendingActions,
    onCreateIndentClick: () -> Unit,
    onCreateTripClick: () -> Unit,
    onInterestInboxClick: () -> Unit,
    onVerificationRequestsClick: (Int, Int, Int, Int) -> Unit,
) {
    Column {
        SectionHeader("QUICK ACTIONS")
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("dashboard_quick_action_create_indent")
                    .semantics { testTagsAsResourceId = true },
                title = "Create Indent",
                icon = Icons.Default.Add,
                // Light: solid forest fill (the one strong CTA). Dark: calm
                // elevated surface with mint accents — a bright 80-tone fill
                // would read as a headlight against the charcoal night UI.
                containerColor = if (isDarkTheme()) {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                } else {
                    MaterialTheme.colorScheme.primary
                },
                contentColor = if (isDarkTheme()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onPrimary
                },
                iconWellColor = if (isDarkTheme()) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                } else {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f)
                },
                onClick = onCreateIndentClick
            )
            QuickActionCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("dashboard_quick_action_adhoc_truck")
                    .semantics { testTagsAsResourceId = true },
                title = "Adhoc Truck",
                icon = Icons.Default.LocalShipping,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                onClick = onCreateTripClick
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("dashboard_quick_action_interest_inbox")
                    .semantics { testTagsAsResourceId = true },
                title = "Interest Inbox",
                icon = Icons.Default.Inbox,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.secondary,
                badgeCount = attentionNeeded.truckerInterests,
                badgeTone = BoundaryBadgeTone.Info,
                onClick = onInterestInboxClick
            )
            QuickActionCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("dashboard_quick_action_verification_requests")
                    .semantics { testTagsAsResourceId = true },
                title = "Verification Requests",
                icon = Icons.Default.Shield,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                badgeCount = pendingActions.verificationRequests.total,
                badgeTone = BoundaryBadgeTone.Urgent,
                onClick = {
                    onVerificationRequestsClick(
                        pendingActions.verificationRequests.tripVerificationRequests,
                        pendingActions.verificationRequests.truckVerificationRequests,
                        pendingActions.verificationRequests.truckOwnerVerificationRequests,
                        pendingActions.verificationRequests.clientVerificationRequests,
                    )
                }
            )
        }
    }
}

@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    iconWellColor: Color? = null,
    badgeCount: Int? = null,
    badgeTone: BoundaryBadgeTone = BoundaryBadgeTone.Info,
    onClick: () -> Unit
) {
    // Root = Box so the badge can be a sibling drawn on top of the card,
    // half-breaking its top-end corner. No clipToBounds anywhere here — the
    // badge overhang must stay visible (the grid root is a Row, not a grid).
    Box(modifier = modifier) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = iconWellColor ?: MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = contentColor)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            }
        }

        if (badgeCount != null) {
            BoundaryBadge(
                count = badgeCount,
                tone = badgeTone,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 12.dp, y = (-12).dp)
                    .testTag("dashboard_quick_action_badge")
                    .semantics { testTagsAsResourceId = true },
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

/** Theme-mode probe: surfaces go dark (< 0.5 luminance) in the night scheme. */
@Composable
private fun isDarkTheme(): Boolean =
    MaterialTheme.colorScheme.surface.luminance() < 0.5f
