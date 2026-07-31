package com.freight24.connect.feature.indents.views

import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.VisibilityChip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.freight24.connect.feature.indents.domain.viewmodel.CreateIndentEvent
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModelImpl
import com.freight24.connect.navigation.ConnectDestination
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewIndentScreen(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    navController: NavController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val createIndentBackStackEntry = remember(navBackStackEntry) {
        navController.getBackStackEntry(ConnectDestination.CreateIndent.route)
    }
    val viewModel: IndentsViewModel = hiltViewModel<IndentsViewModelImpl>(
        createIndentBackStackEntry
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            uiState.createdIndentId?.let { indentId ->
                navController.navigate(
                    ConnectDestination.IndentSuccess.createRoute(indentId)
                ) {
                    launchSingleTop = true
                }
                viewModel.onEvent(CreateIndentEvent.ClearSuccess)
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CreateIndentEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = { Text("Review Indent") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onEditClick) {
                        Text("Edit", color = MaterialTheme.colorScheme.primary)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp, top = 10.dp)
            ) {
                Button(
                    onClick = { viewModel.onEvent(CreateIndentEvent.SubmitIndent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    enabled = !uiState.isSubmitting,
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp).padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp).padding(end = 8.dp),
                        )
                    }
                    Text("Confirm & Create Indent")
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Please review details before finalizing.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ReviewGeneralInfoCard(
                clientName = uiState.selectedClient?.companyName ?: "Not selected",
                tagCode = uiState.selectedClient?.tagCode,
                onEditClick = onEditClick,
            )

            ReviewRouteCard(
                originCity = uiState.selectedFromLocation?.let {
                    "${it.name}${it.state?.let { s -> ", $s" } ?: ""}"
                } ?: "Not selected",
                destinationCity = uiState.selectedToLocation?.let {
                    "${it.name}${it.state?.let { s -> ", $s" } ?: ""}"
                } ?: "Not selected",
                onEditClick = onEditClick,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReviewFinancialsCard(
                    clientCost = uiState.clientCost,
                    negotiatedCost = uiState.negotiatedCost,
                    hideCost = uiState.hideCost,
                    onEditClick = onEditClick,
                    modifier = Modifier.weight(1f),
                )
                ReviewSpecsCard(
                    weight = uiState.weight.ifBlank { "0" },
                    vehicleType = uiState.selectedTruckType?.displayName,
                    onEditClick = onEditClick,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReviewTatMaterialCard(
                    tatHours = uiState.tatHours,
                    tatRequired = uiState.tatRequired,
                    materialType = uiState.materialName,
                    modifier = Modifier.weight(1f),
                )
            }

            if (uiState.remarks.isNotEmpty()) {
                ReviewRemarksCard(
                    remarks = uiState.remarks,
                    onEditClick = onEditClick,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Compact detail card shell
// ---------------------------------------------------------------------------

@Composable
private fun CompactDetailCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onEditClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val cs = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = cs.surfaceContainerHigh,
        border = BorderStroke(1.dp, cs.outlineVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(cs.primaryContainer.copy(alpha = 0.35f), cs.surfaceContainerHigh)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = cs.primaryContainer,
                        contentColor = cs.onPrimaryContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, null, Modifier.size(13.dp))
                        }
                    }
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = cs.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.weight(1f),
                    )
                    if (onEditClick != null) {
                        Surface(
                            onClick = onEditClick,
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = cs.errorContainer,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Edit, "Edit",
                                    tint = cs.onErrorContainer,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                }
                content()
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Section cards
// ---------------------------------------------------------------------------

@Composable
private fun ReviewGeneralInfoCard(
    clientName: String,
    tagCode: String?,
    onEditClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    CompactDetailCard(Icons.Default.Info, "General Info", onEditClick = onEditClick) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(clientName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = cs.onSurface)
            Text("Tag: ${tagCode?.takeIf { it.isNotBlank() } ?: "—"}", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReviewRouteCard(
    originCity: String,
    destinationCity: String,
    onEditClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val ext = com.freight24.ui.theme.FreightThemeTokens.colors

    CompactDetailCard(Icons.Default.Route, "Route", onEditClick = onEditClick) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Alignment.Top) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp)) {
                Box(Modifier.size(9.dp).background(ext.routeOrigin, CircleShape))
                Box(Modifier.width(3.dp).height(20.dp).clip(RoundedCornerShape(3.dp)).background(Brush.verticalGradient(listOf(ext.routeOrigin, ext.routeDestination))))
                Box(Modifier.size(9.dp).background(ext.routeDestination, CircleShape))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    DetailLabel("ORIGIN")
                    Text(originCity, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = cs.onSurface)
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    DetailLabel("DESTINATION")
                    Text(destinationCity, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = cs.onSurface)
                }
            }
        }
    }
}

@Composable
private fun ReviewFinancialsCard(
    clientCost: String,
    negotiatedCost: String,
    hideCost: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    CompactDetailCard(Icons.Outlined.Payments, "Financials", modifier, onEditClick = onEditClick) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CostTile("CLIENT", formatInr(clientCost))
            CostTile("NEGOTIATED", formatInr(negotiatedCost), valueColor = cs.primary)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                DetailLabel("VISIBILITY")
                Spacer(Modifier.weight(1f))
                VisibilityChip(isVisible = !hideCost)
            }
        }
    }
}

@Composable
private fun ReviewSpecsCard(
    weight: String,
    vehicleType: String?,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    CompactDetailCard(Icons.Outlined.Tune, "Specs", modifier, onEditClick = onEditClick) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                DetailLabel("WEIGHT")
                Text("$weight MT", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = cs.onSurface)
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                DetailLabel("VEHICLE")
                Text(vehicleType ?: "Not selected", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (vehicleType != null) cs.onSurface else cs.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ReviewTatMaterialCard(
    tatHours: String,
    tatRequired: Boolean,
    materialType: String,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    CompactDetailCard(Icons.Outlined.LocalShipping, "Details", modifier) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                DetailLabel("TAT")
                Text(
                    if (tatRequired && tatHours.isNotBlank()) "$tatHours Hrs" else "No",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (tatRequired) cs.onSurface else cs.onSurfaceVariant,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                DetailLabel("MATERIAL")
                Text(materialType.ifBlank { "—" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = cs.onSurface)
            }
        }
    }
}

@Composable
private fun ReviewRemarksCard(remarks: String, onEditClick: () -> Unit) {
    CompactDetailCard(Icons.Default.Info, "Remarks", onEditClick = onEditClick) {
        Surface(
            Modifier.fillMaxWidth(),
            RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Text(remarks, Modifier.padding(10.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---------------------------------------------------------------------------
// Shared
// ---------------------------------------------------------------------------

@Composable
private fun DetailLabel(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = modifier)
}

@Composable
private fun CostTile(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    val cs = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(10.dp), color = cs.surfaceContainerHighest, border = BorderStroke(1.dp, cs.outlineVariant)) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            DetailLabel(label)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

private fun formatInr(amount: String): String {
    val number = amount.replace(",", "").toDoubleOrNull()
    return if (number != null) {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
        format.maximumFractionDigits = 0
        format.format(number).replace("₹", "₹ ")
    } else {
        "₹ $amount"
    }
}
