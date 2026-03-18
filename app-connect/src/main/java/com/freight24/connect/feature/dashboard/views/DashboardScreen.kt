package com.freight24.connect.feature.dashboard.views

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.freight24.connect.feature.dashboard.domain.viewmodel.DashboardEvent
import com.freight24.connect.feature.dashboard.domain.viewmodel.DashboardViewModel
import com.freight24.connect.feature.dashboard.domain.viewmodel.DashboardViewModelImpl
import com.freight24.rest.response.DashboardDataResponse

@Composable
fun DashboardScreen(
    paddingValues: PaddingValues,
    viewModel: DashboardViewModel = hiltViewModel<DashboardViewModelImpl>(),
    onCreateIndentClick: () -> Unit = {},
    onCreateTripClick: () -> Unit = {},
    onInterestInboxClick: () -> Unit = {},
    onActiveTripsClick: () -> Unit = {}
) {
    val uiState = viewModel.uiState
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color(0xFFF8FAFC))
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (uiState.error != null) {
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
                    AttentionNeededSection(data.attentionNeeded)
                    IndentPulseSection(data.indentPulse)
                    ActiveTripsSection(data.activeTrips)
                    PendingActionsSection(data.pendingActions)
                    QuickActionsSection(
                        onCreateIndentClick = onCreateIndentClick,
                        onCreateTripClick = onCreateTripClick,
                        onInterestInboxClick = onInterestInboxClick,
                        onActiveTripsClick = onActiveTripsClick
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun AttentionNeededSection(attentionNeeded: DashboardDataResponse.AttentionNeeded) {
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
                title = "Trucker\nInterview",
                value = "${attentionNeeded.truckerInterests} New Leads",
                backgroundColor = Color(0xFFEAFBF3),
                valueColor = Color(0xFF047857)
            )
            // Waiting for LR Card
            AttentionCard(
                modifier = Modifier.weight(1f),
                title = "Waiting for LR",
                value = "${attentionNeeded.waitingForLR} Pending",
                backgroundColor = Color(0xFFFFF7ED),
                valueColor = Color(0xFFEA580C)
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
    valueColor: Color
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
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
fun IndentPulseSection(indentPulse: DashboardDataResponse.IndentPulse) {
    Column {
        SectionHeader("INDENT PULSE")
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PulseCard(modifier = Modifier.weight(1f), title = "TOTAL", count = indentPulse.total.toString(), color = Color(0xFF1E293B))
            PulseCard(modifier = Modifier.weight(1f), title = "CLOSED", count = indentPulse.closed.toString(), color = Color(0xFF16A34A))
            PulseCard(modifier = Modifier.weight(1f), title = "OPEN", count = indentPulse.open.toString(), color = Color(0xFF2563EB))
        }
    }
}

@Composable
fun PulseCard(modifier: Modifier = Modifier, title: String, count: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
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
fun ActiveTripsSection(activeTrips: DashboardDataResponse.ActiveTrips) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
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
            
            TripProgressRow("ASSIGNED", "${activeTrips.assigned} TRIP", activeTrips.assigned / safeTotal, Color(0xFF14B8A6))
            TripProgressRow("LOADING", "${activeTrips.loading} TRIP", activeTrips.loading / safeTotal, Color(0xFFF97316))
            TripProgressRow("IN-TRANSIT", "${activeTrips.inTransit} TRIPS", activeTrips.inTransit / safeTotal, Color(0xFF10B981))
        }
    }
}

@Composable
fun TripProgressRow(label: String, countText: String, progress: Float, color: Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
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
fun PendingActionsSection(pendingActions: DashboardDataResponse.PendingActions) {
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
                badgeColor = Color(0xFF10B981)
            )
            ActionCard(
                icon = Icons.Default.VerticalSplit, // A box icon placeholder
                iconBackgroundColor = Color(0xFFFEE2E2),
                iconTintColor = Color(0xFFB91C1C),
                title = "POD Follow-up",
                subtitle = "${pendingActions.podFollowUp} action required",
                badgeColor = Color(0xFFEF4444)
            )
        }
    }
}

@Composable
fun ActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBackgroundColor: Color,
    iconTintColor: Color,
    title: String,
    subtitle: String,
    badgeColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
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
    onCreateIndentClick: () -> Unit,
    onCreateTripClick: () -> Unit,
    onInterestInboxClick: () -> Unit,
    onActiveTripsClick: () -> Unit
) {
    Column {
        SectionHeader("QUICK ACTIONS")
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                title = "Create Indent",
                icon = Icons.Default.Add,
                backgroundColor = Color(0xFF0D9488), // Teal green solid
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
                badgeText = "2",
                onClick = onInterestInboxClick
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                title = "Active Trips",
                icon = Icons.Default.LocationOn,
                backgroundColor = Color.White,
                contentColor = Color(0xFF0D9488),
                onClick = onActiveTripsClick
            )
        }
    }
}

@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (backgroundColor == Color.White) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)) else null
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
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
            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-16).dp, y = (-8).dp)
                        .background(Color.Red, CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        badgeText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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

