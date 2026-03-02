package com.freight24.connect.feature.trips.views

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
            TopAppBar(
                title = { Text("Halting Charges", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
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
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    enabled = !uiState.isUpdating && uiState.hasChanges
                ) {
                    if (uiState.isUpdating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Update Halting Charges", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
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
                color = Color(0xFFECFDF5),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Halting charges are subject to verification by the logistics manager. Please provide accurate details and supporting documents.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF334155)
                    )
                }
            }

            // Halting Days Section
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        BreakdownItem("Daily Halting Rate", "₹${uiState.dailyRate}")
                        Divider(color = Color(0xFFF1F5F9))
                        BreakdownItem("Total Days", "${uiState.loadingDays + uiState.unloadingDays} Days")
                        Divider(color = Color(0xFFF1F5F9))
                        Surface(color = Color(0xFFF8FAFC)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Halting Charge", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                Text("₹${(uiState.loadingDays + uiState.unloadingDays) * uiState.dailyRate}", color = Color(0xFF10B981), fontWeight = FontWeight.ExtraBold)
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
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFFE2E8F0),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
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
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onDecrement,
                modifier = Modifier.size(36.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
            }
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onIncrement,
                modifier = Modifier.size(36.dp).background(Color(0xFF10B981), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
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
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
    }
}
