package com.freight24.connect.feature.indents.views

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.indents.domain.viewmodel.IndentTab
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsListEvent
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsListUiState
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsListViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsListViewModelImpl
import com.freight24.rest.response.IndentResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndentsScreen(
    onIndentClick: (String) -> Unit = {},
    onCloneClick: (String, String) -> Unit = { s1, s2 -> },
    onSearchClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: IndentsListViewModel = hiltViewModel<IndentsListViewModelImpl>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) { _ ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.onEvent(IndentsListEvent.RefreshIndents) },
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
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab Switcher
            IndentTabSwitcher(
                selectedTab = uiState.selectedTab,
                onTabSelected = { onEvent(IndentsListEvent.SelectTab(it)) },
                modifier = Modifier.padding(16.dp)
            )

            if (uiState.selectedTab == IndentTab.ALL) {
                SearchBarTrigger(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                )
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
                    selectedTab = uiState.selectedTab,
                    onIndentClick = onIndentClick,
                    onCloneClick = onCloneClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun SearchBarTrigger(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Search by Client or Indent ID",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun IndentTabSwitcher(
    selectedTab: IndentTab,
    onTabSelected: (IndentTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
    ) {
        TabButton(
            text = "Mine",
            isSelected = selectedTab == IndentTab.MINE,
            onClick = { onTabSelected(IndentTab.MINE) },
            modifier = Modifier.width(100.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        TabButton(
            text = "All",
            isSelected = selectedTab == IndentTab.ALL,
            onClick = { onTabSelected(IndentTab.ALL) },
            modifier = Modifier.width(100.dp)
        )
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(24.dp),
        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = modifier.height(32.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        )
    }
}

@Composable
fun IndentsList(
    indents: List<IndentResponse.Indent>,
    selectedTab: IndentTab,
    onIndentClick: (String) -> Unit,
    onCloneClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
    ) {
        items(indents) { indent ->
            when (selectedTab) {
                IndentTab.MINE -> IndentCard(
                    indent = indent,
                    onClick = { onIndentClick(indent.id) }
                )
                IndentTab.ALL -> AllIndentCard(
                    indent = indent,
                    onClick = { onIndentClick(indent.id) },
                    onCloneClick = { onCloneClick(indent.id, indent.indentGroupDetails.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndentCard(
    indent: IndentResponse.Indent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loadingLocation = indent.indentGroupDetails.indentLocations.find { it.locationType == "loading" }
    val unloadingLocation = indent.indentGroupDetails.indentLocations.find { it.locationType == "unloading" }
    val clientProfile = indent.indentGroupDetails.clientProfile
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Row 1: Route & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RouteDot(isStart = true)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = loadingLocation?.areaName ?: loadingLocation?.cityName ?: "Unknown",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                    RouteVerticalLine()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RouteDot(isStart = false)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = unloadingLocation?.areaName ?: unloadingLocation?.cityName ?: "Unknown",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
                
                // Posted time
                Text(
                    text = "45m ago", // TODO: Dynamic formatting
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Metadata (Truck, Weight, Material)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocalShipping,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                val truckDisplay = indent.indentGroupDetails.truckType?.displayName ?: "N/A"
                val weightDisplay = "${indent.indentGroupDetails.loadWeight} MT"
                val materialDisplay = indent.indentGroupDetails.material
                Text(
                    text = "$truckDisplay • $weightDisplay • $materialDisplay",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Pricing
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CLIENT: ",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(indent.indentGroupDetails.clientTripCost),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "NEG: ",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(indent.negotiatedTripCost),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Row 4: Client Info & Call Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = clientProfile?.fullName ?: clientProfile?.companyName ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        clientProfile?.phoneNumber?.let {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$it")
                            }
                            context.startActivity(intent)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Call",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllIndentCard(
    indent: IndentResponse.Indent,
    onClick: () -> Unit,
    onCloneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loadingLocation = indent.indentGroupDetails.indentLocations.find { it.locationType == "loading" }
    val unloadingLocation = indent.indentGroupDetails.indentLocations.find { it.locationType == "unloading" }
    val clientProfile = indent.indentGroupDetails.clientProfile
    val fleetExecutive = indent.fleetExecutive

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Row 1: Company Name, ID & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocalShipping,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = clientProfile?.companyName ?: clientProfile?.fullName ?: "Unknown Company",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = indent.indentCode.orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
                
                // Posted time
                Text(
                    text = "45m ago(hardcoded)", // TODO: Dynamic formatting
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Route
            Row(verticalAlignment = Alignment.CenterVertically) {
                RouteDot(isStart = true)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = loadingLocation?.areaName ?: loadingLocation?.cityName ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }
            RouteVerticalLine()
            Row(verticalAlignment = Alignment.CenterVertically) {
                RouteDot(isStart = false)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = unloadingLocation?.areaName ?: unloadingLocation?.cityName ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Metadata (Truck, Weight, Material)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocalShipping,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                val truckDisplay = indent.indentGroupDetails.truckType?.displayName ?: "N/A"
                val weightDisplay = "${indent.indentGroupDetails.loadWeight} tons"
                val materialDisplay = indent.indentGroupDetails.material
                Text(
                    text = "$truckDisplay | $weightDisplay | $materialDisplay",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 4: Pricing
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CLIENT: ",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(indent.indentGroupDetails.clientTripCost),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "NEG: ",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(indent.negotiatedTripCost),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Row 5: Client Name & Clone Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = fleetExecutive?.name ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if(indent.indentOwnership == "OTHER") {
                    Button(
                        onClick = onCloneClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.LocalShipping,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Clone",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun RouteDot(isStart: Boolean) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(
                color = if (isStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            )
    )
}

@Composable
fun RouteVerticalLine() {
    Box(
        modifier = Modifier
            .padding(start = 3.dp) // center under 8dp dot
            .width(4.dp)
            .height(12.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
fun CostColumn(
    label: String,
    value: String,
    alignment: Alignment.Horizontal = Alignment.Start,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = alignment
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun formatCurrency(amount: Double?): String {
    if (amount == null) return "₹ 0"
    return "₹ ${"%,.0f".format(amount)}"
}
