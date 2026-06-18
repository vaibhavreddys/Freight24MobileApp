package com.freight24.connect.feature.dashboard.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import com.freight24.connect.feature.dashboard.domain.viewmodel.DashboardEvent
import com.freight24.connect.feature.dashboard.domain.viewmodel.DashboardViewModel
import com.freight24.connect.feature.dashboard.domain.viewmodel.DashboardViewModelImpl
import com.freight24.connect.feature.trips.domain.usecase.TripTypes
import com.freight24.rest.response.DashboardDataResponse

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
    onRefresh: () -> Unit = { viewModel.onEvent(DashboardEvent.Refresh) }
) {
    val uiState = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()
    val lifecycleOwner = LocalLifecycleOwner.current
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
                        RefreshProgressBar(
                            isRefreshing = uiState.isRefreshing,
                            color = colorScheme.primary,
                        )
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
            // Payment Enquiry (Mocked for UI)
            AttentionCard(
                modifier = Modifier.weight(1f),
                title = "Payment\nEnquiry",
                value = "1 Urgent",
                backgroundColor = colorScheme.errorContainer,
                valueColor = colorScheme.error
            )
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                value,
                style = MaterialTheme.typography.labelMedium,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                badgeColor = MaterialTheme.colorScheme.primary,
                onClick = { onTripFilterClick(TripTypes.LR_PENDING_REVIEW) }
            )
            ActionCard(
                icon = Icons.Default.VerticalSplit, // A box icon placeholder
                iconBackgroundColor = MaterialTheme.colorScheme.errorContainer,
                iconTintColor = MaterialTheme.colorScheme.error,
                title = "POD Follow-up",
                subtitle = "${pendingActions.podFollowUp} action required",
                badgeColor = MaterialTheme.colorScheme.error,
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
    badgeColor: Color,
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
                shape = RoundedCornerShape(12.dp),
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
            Box(
                modifier = Modifier.size(6.dp).background(badgeColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
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
                modifier = Modifier.weight(1f),
                title = "Create Indent",
                icon = Icons.Default.Add,
                backgroundColor = null,
                backgroundBrush = Brush.linearGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                ),
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = onCreateIndentClick
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                title = "Adhoc Truck",
                icon = Icons.Default.LocalShipping,
                backgroundColor = MaterialTheme.colorScheme.surface,
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
                modifier = Modifier.weight(1f),
                title = "Interest Inbox",
                icon = Icons.Default.Inbox,
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.secondary,
                badgeText = attentionNeeded.truckerInterests.toString(),
                onClick = onInterestInboxClick
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                title = "Verification Requests",
                icon = Icons.Default.Shield,
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                badgeText = pendingActions.verificationRequests.total.toString(),
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
    backgroundColor: Color? = null,
    backgroundBrush: Brush? = null,
    contentColor: Color,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Box(modifier = modifier) {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (backgroundBrush != null) MaterialTheme.colorScheme.surface.copy(alpha = 0f) else backgroundColor ?: MaterialTheme.colorScheme.surface
                ),
            border = if (backgroundColor == MaterialTheme.colorScheme.surface) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
            ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (backgroundBrush != null) Modifier.background(backgroundBrush) else Modifier)
                    .padding(vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = if (backgroundColor == MaterialTheme.colorScheme.surface) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = contentColor)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                }
            }
        }
        
        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    badgeText,
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}


@Composable
private fun RefreshProgressBar(
    isRefreshing: Boolean,
    color: Color,
) {
    // Drives the determinate fill value. Animatable gives us a suspending,
    // cancellable animation that we can re-target from the LaunchedEffect
    // below, and its .value is read directly by the LinearProgressIndicator.
    val progress = remember { Animatable(0f) }
    // Controls fade-in / fade-out so the bar doesn't appear or disappear
    // abruptly. Toggled explicitly by the effect.
    var visible by remember { mutableStateOf(false) }

    // Single LaunchedEffect orchestrates the three phases:
    //   - isRefreshing == true  -> fade in, crawl 0 -> 0.9 (loop, capped)
    //   - isRefreshing -> false -> glide to 1f, hold 400ms, fade out
    // Re-keying on isRefreshing cancels the in-flight coroutine, so we always
    // end up in the correct phase for the latest state.
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            visible = true
            progress.snapTo(0f)
            // Indeterminate-style crawl. The API duration is unknown, so we
            // never quite reach 1f while the request is in flight — the bar
            // caps at 0.9 and the API completion is what actually pushes it
            // to 1f in the next phase.
            while (isRefreshing) {
                progress.animateTo(
                    targetValue = 0.9f,
                    animationSpec = tween(durationMillis = 3000, easing = LinearEasing),
                )
                if (!isRefreshing) break
                progress.snapTo(0f)
            }
        } else if (visible) {
            // Completion: glide to 1f, hold briefly so the user sees the bar
            // reach the end, then drop out of the AnimatedVisibility so it
            // fades away.
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 150, easing = LinearEasing),
            )
            delay(400)
            visible = false
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 150)),
        exit = fadeOut(animationSpec = tween(durationMillis = 250)),
    ) {
        LinearProgressIndicator(
            progress = { progress.value },
            modifier = Modifier.fillMaxWidth(),
            color = color,
        )
    }
}
