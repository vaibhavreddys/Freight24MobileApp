package com.freight24.connect.feature.trucks.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.trucks.domain.viewmodel.AssignTruckEvent
import com.freight24.connect.feature.trucks.domain.viewmodel.AssignTruckViewModel
import com.freight24.connect.feature.trucks.enums.TruckStatus
import com.freight24.rest.response.SearchTruckResponse
import com.freight24.rest.utils.isEnabled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignTruckScreen(
    onBackClick: () -> Unit,
    onTruckSelected: (String, String) -> Unit, // Callback with Truck ID and Registration Number
    viewModel: AssignTruckViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assign Truck", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5) // Light gray background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { viewModel.onEvent(AssignTruckEvent.UpdateQuery(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp)),
                placeholder = { 
                    Text(
                        "Enter vehicle no. (e.g. KA01AB1234)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    ) 
                },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Search, 
                        contentDescription = "Search", 
                        tint = Color(0xFF94A3B8)
                    ) 
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFFE2E8F0),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    disabledBorderColor = Color(0xFFF1F5F9)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Results Header
            if (uiState.trucks.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "SEARCH RESULTS",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE2E8F0)
                    ) {
                        Text(
                            "${uiState.trucks.size} TRUCKS FOUND",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Results List
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.errorMessage!!, color = Color.Red)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.trucks) { truck ->
                        TruckItem(truck = truck, onSelect = { onTruckSelected(truck.truckId, truck.registrationNumber) })
                    }
                }
            }
        }
    }
}

@Composable
fun TruckItem(
    truck: SearchTruckResponse.Truck,
    onSelect: () -> Unit
) {
    val isEnabled = truck.isEnabled()
    val statusText = TruckStatus.from(truck).displayText
    val statusColor = if (isEnabled) Color(0xFF22C55E) else Color(0xFFEF4444)
    val buttonColor = if (isEnabled) Color(0xFF22C55E) else Color(0xFFE2E8F0)
    val buttonContentColor = if (isEnabled) Color.White else Color(0xFF94A3B8)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Truck Icon Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = "Truck",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Truck Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = truck.registrationNumber,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = truck.owner.tagCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = truck.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
                
                Text(
                    text = "Owner: ${truck.owner.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Select Button
            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = buttonContentColor,
                    disabledContainerColor = buttonColor,
                    disabledContentColor = buttonContentColor
                ),
                enabled = isEnabled,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Select",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Extension to capitalize first letter
fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
