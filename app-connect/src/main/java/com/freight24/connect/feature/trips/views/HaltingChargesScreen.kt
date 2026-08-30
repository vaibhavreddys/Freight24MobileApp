package com.freight24.connect.feature.trips.views
import com.freight24.connect.feature.trips.views.components.formatAmount
import com.freight24.ui.components.FreightTopAppBar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.trips.domain.viewmodel.HaltingChargesEvent
import com.freight24.connect.feature.trips.domain.viewmodel.HaltingChargesViewModel
import com.freight24.connect.feature.trips.domain.viewmodel.HaltingChargesViewModelImpl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HaltingChargesScreen(
    tripId: String,
    truckTypeId: String,
    initialLoadingDays: Int,
    initialUnloadingDays: Int,
    notes: String,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: HaltingChargesViewModel = hiltViewModel<HaltingChargesViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(truckTypeId) {
        viewModel.onEvent(HaltingChargesEvent.LoadHaltingRate(truckTypeId, initialLoadingDays, initialUnloadingDays, notes))
    }

    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = { Text("Halting Charges", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { viewModel.onEvent(HaltingChargesEvent.SubmitHaltingCharges(tripId)) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    enabled = !uiState.isUpdating && uiState.hasChanges
                ) {
                    if (uiState.isUpdating) {
                        CircularProgressIndicator(color = colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Update Halting Charges", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Info Message
            Surface(
                color = colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
                border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Halting charges are subject to verification by the logistics manager. Please provide accurate details and supporting documents.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            // Halting Days Section
            Card(
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Halting Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    DayCounterRow(
                        label = "Loading No of Days",
                        count = uiState.loadingDays,
                        onIncrement = { viewModel.onEvent(HaltingChargesEvent.UpdateLoadingDays(uiState.loadingDays + 1)) },
                        onDecrement = { viewModel.onEvent(HaltingChargesEvent.UpdateLoadingDays(uiState.loadingDays - 1)) }
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    DayCounterRow(
                        label = "Unloading No of Days",
                        count = uiState.unloadingDays,
                        onIncrement = { viewModel.onEvent(HaltingChargesEvent.UpdateUnloadingDays(uiState.unloadingDays + 1)) },
                        onDecrement = { viewModel.onEvent(HaltingChargesEvent.UpdateUnloadingDays(uiState.unloadingDays - 1)) }
                    )
                }
            }

            // Charge Breakdown Section
            Column {
                Text("Charge Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        BreakdownItem("Daily Halting Rate", "₹${formatAmount(uiState.dailyRate.toString())}")
                        HorizontalDivider(color = colorScheme.outlineVariant)
                        BreakdownItem("Total Days", "${uiState.loadingDays + uiState.unloadingDays} Days")
                        HorizontalDivider(color = colorScheme.outlineVariant)
                        Surface(color = colorScheme.surfaceVariant) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Halting Charge", color = colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text("₹${formatAmount(((uiState.loadingDays + uiState.unloadingDays) * uiState.dailyRate).toString())}", color = colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }

            // Notes Section
            Column {
                Text("Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.onEvent(HaltingChargesEvent.UpdateNotes(it)) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("Add any additional notes or details here...", style = MaterialTheme.typography.bodyMedium) },
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface,
                        focusedBorderColor = colorScheme.outlineVariant,
                        unfocusedBorderColor = colorScheme.outlineVariant
                    )
                )
            }
        }
    }
}

@Composable
fun DayCounterRow(
    label: String,
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onDecrement,
                modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onIncrement,
                modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun BreakdownItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
