package com.freight24.connect.feature.dashboard.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
    onVerificationRequestsClick: () -> Unit = {},
    onTripFilterClick: (TripTypes) -> Unit = {},
    onOpenIndentsClick: () -> Unit = {},
    onActiveAssignedClick: () -> Unit = {},
    onActiveLoadingClick: () -> Unit = {},
    onActiveInTransitClick: () -> Unit = {},
    onRefresh: () -> Unit = { viewModel.onEvent(DashboardEvent.Refresh) }
) {
    val uiState = viewModel.uiState
    val scrollState = rememberScrollState()

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color(0xFFF8FAFC))
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
                            onCreateIndentClick = onCreateIndentClick,
                            onCreateTripClick = onCreateTripClick,
                            onInterestInboxClick = onInterestInboxClick,
                            onVerificationRequestsClick = onVerificationRequestsClick
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
                backgroundColor = Color(0xFFEAFBF3),
                valueColor = Color(0xFF047857),
                onClick = onInterestInboxClick
            )
            // Waiting for LR Card
            AttentionCard(
                modifier = Modifier.weight(1f),
                title = "Waiting for LR",
                value = "${attentionNeeded.waitingForLR} Pending",
                backgroundColor = Color(0xFFFFF7ED),
                valueColor = Color(0xFFEA580C),
                onClick = { onTripFilterClick(TripTypes.LR_NOT_UPLOADED) }
            )
            // Payment Enquiry (Mocked for UI)
            AttentionCard(
                modifier = Modifier.weight(1f),
                title = "Payment\nEnquiry",
                value = "1 Urgent",
                backgroundColor = Color(0xFFFEF2F2),
                valueColor = Color(0xFFDC2626)
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
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF334155),
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
            PulseCard(modifier = Modifier.weight(1f), title = "TOTAL", count = indentPulse.total.toString(), color = Color(0xFF1E293B))
            PulseCard(modifier = Modifier.weight(1f), title = "CLOSED", count = indentPulse.closed.toString(), color = Color(0xFF16A34A))
            PulseCard(modifier = Modifier.weight(1f), title = "OPEN", count = indentPulse.open.toString(), color = Color(0xFF2563EB), onClick = onOpenClick)
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF64748B),
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
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
                    color = Color(0xFF0F172A)
                )
                Text(
                    "${activeTrips.total} Total",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF047857)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress Bars
            val safeTotal = if (activeTrips.total > 0) activeTrips.total.toFloat() else 1f
            
            TripProgressRow("ASSIGNED", "${activeTrips.assigned} TRIP", activeTrips.assigned / safeTotal, Color(0xFF14B8A6), onAssignedClick)
            TripProgressRow("LOADING", "${activeTrips.loading} TRIP", activeTrips.loading / safeTotal, Color(0xFFF97316), onLoadingClick)
            TripProgressRow("IN-TRANSIT", "${activeTrips.inTransit} TRIPS", activeTrips.inTransit / safeTotal, Color(0xFF10B981), onInTransitClick)
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
                color = Color(0xFF475569),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                countText,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF475569),
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFFF1F5F9),
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
                iconBackgroundColor = Color(0xFFDCFCE7),
                iconTintColor = Color(0xFF15803D),
                title = "LR waiting for review",
                subtitle = "${pendingActions.lrReviews} documents pending",
                badgeColor = Color(0xFF10B981),
                onClick = { onTripFilterClick(TripTypes.LR_PENDING_REVIEW) }
            )
            ActionCard(
                icon = Icons.Default.VerticalSplit, // A box icon placeholder
                iconBackgroundColor = Color(0xFFFEE2E2),
                iconTintColor = Color(0xFFB91C1C),
                title = "POD Follow-up",
                subtitle = "${pendingActions.podFollowUp} action required",
                badgeColor = Color(0xFFEF4444),
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
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
                    color = Color(0xFF1E293B)
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(6.dp).background(badgeColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun QuickActionsSection(
    attentionNeeded: DashboardDataResponse.AttentionNeeded,
    onCreateIndentClick: () -> Unit,
    onCreateTripClick: () -> Unit,
    onInterestInboxClick: () -> Unit,
    onVerificationRequestsClick: () -> Unit
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
                backgroundColor = Color.Transparent,
                backgroundBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFF10B981), Color(0xFF0D9488))
                ),
                contentColor = Color.White,
                onClick = onCreateIndentClick
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                title = "Create Trip",
                icon = Icons.Default.LocalShipping,
                backgroundColor = Color.White,
                contentColor = Color(0xFF0D9488),
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
                backgroundColor = Color.White,
                contentColor = Color(0xFFE11D48), // Pinkish red
                badgeText = attentionNeeded.truckerInterests.toString(),
                onClick = onInterestInboxClick
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                title = "Verification Requests",
                icon = Icons.Default.Shield,
                backgroundColor = Color.White,
                contentColor = Color(0xFF0D9488),
                onClick = onVerificationRequestsClick
            )
        }
    }
}

@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    backgroundColor: Color = Color.Transparent,
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
                containerColor = if (backgroundBrush != null) Color.Transparent else backgroundColor
            ),
            border = if (backgroundColor == Color.White) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null
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
                        color = if (backgroundColor == Color.White) Color(0xFFF1F5F9) else Color(0x33FFFFFF),
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
                    .background(Color(0xFFEF4444), CircleShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    badgeText,
                    color = Color.White,
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
        color = Color(0xFF64748B),
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
