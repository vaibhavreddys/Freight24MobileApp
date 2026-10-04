package com.freight24.connect.feature.trips.views
import com.freight24.ui.components.FreightTopAppBar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.freight24.connect.feature.trips.domain.viewmodel.OtherDeductionsEvent
import com.freight24.connect.feature.trips.domain.viewmodel.OtherDeductionsViewModel
import com.freight24.connect.feature.trips.domain.viewmodel.OtherDeductionsViewModelImpl
import com.freight24.ui.components.formViewportInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherDeductionsScreen(
    tripId: String,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: OtherDeductionsViewModel = hiltViewModel<OtherDeductionsViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = { Text("Other Deductions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Button(
                onClick = { viewModel.onEvent(OtherDeductionsEvent.OnUpdateClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                enabled = !uiState.isLoading && uiState.hasChanges
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update Deductions", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Keyboard-aware viewport: without this the scroll bounds run behind the
                // keyboard and bring-into-view leaves the focused field hidden. See FormInsets.kt.
                .formViewportInsets(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // NOTE box
            Surface(
                color = colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().border(1.dp, colorScheme.primary.copy(alpha = 0.2f), MaterialTheme.shapes.medium)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "These are miscellaneous deductions subject to approval by the finance department. Please provide accurate details.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            // Deduction Amount
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Deduction Amount",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.onSurface
                )
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = { viewModel.onEvent(OtherDeductionsEvent.OnAmountChange(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00") },
                    leadingIcon = {
                        Text("₹", color = colorScheme.primary, fontWeight = FontWeight.Bold)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outlineVariant,
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface
                    ),
                    singleLine = true
                )
            }

            // Reason for Deduction
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Reason for Deduction",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.onSurface
                )
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.onEvent(OtherDeductionsEvent.OnReasonChange(it)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    placeholder = { 
                        Text(
                            "Enter the specific reason or notes for this deduction...",
                            color = colorScheme.onSurfaceVariant
                        ) 
                    },
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outlineVariant,
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface
                    ),
                    minLines = 4
                )
            }
        }
    }
}
