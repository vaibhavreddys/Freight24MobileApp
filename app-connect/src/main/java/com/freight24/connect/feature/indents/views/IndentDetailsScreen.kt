package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndentDetailsScreen(
    indentId: String,
    onBackClick: () -> Unit,
) {
    // In a real implementation, you would use a ViewModel to fetch indent details by ID
    // val viewModel: IndentDetailsViewModel = hiltViewModel()
    // val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Indent Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Reusing components from IndentDetailsComponents.kt
            
            GeneralInfoSection(
                clientName = "Sample Client Ltd.", // Replace with data from ViewModel
                tagCode = "TAG-789",
                onEditClick = null // Edit is not available in details view
            )

            RouteDetailsSection(
                originCity = "Mumbai, Maharashtra",
                destinationCity = "Delhi, NCR",
                onEditClick = null
            )

            FinancialDetailsSection(
                clientCost = "45,000",
                negotiatedCost = "42,500",
                hideCost = false,
                onEditClick = null
            )

            SpecificationsSection(
                weight = "15",
                materialType = "Industrial Goods",
                truckType = "32ft SXL",
                tatRequired = true,
                tatHours = "72",
                tarpaulinRequired = true,
                onEditClick = null
            )

            RemarksSection(
                remarks = "Please ensure delivery before Monday morning.",
                onEditClick = null
            )
        }
    }
}
