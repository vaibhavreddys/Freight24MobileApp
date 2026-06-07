package com.freight24.connect.feature.indents.views
import com.freight24.ui.components.FreightTopAppBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.freight24.connect.feature.indents.domain.viewmodel.CreateIndentEvent
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModelImpl
import com.freight24.connect.navigation.ConnectDestination

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
                    // Keep CreateIndent in backstack so navigation can access its ViewModel
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
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    enabled = !uiState.isSubmitting,
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp).padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    }
                    Text("Confirm & Create Indent")
                }
            }
        },
    ) { paddingValues ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Please review the indent details before finalizing",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // General Info Section
            GeneralInfoSection(
                clientName = uiState.selectedClient?.companyName ?: "Not selected",
                tagCode = uiState.selectedClient?.tagCode,
                onEditClick = onEditClick
            )

            // Route Details Section
            RouteDetailsSection(
                originCity = uiState.selectedFromLocation?.let {
                    "${it.name}${it.state?.let { state -> ", $state" } ?: ""}"
                } ?: "Not selected",
                destinationCity = uiState.selectedToLocation?.let {
                    "${it.name}${it.state?.let { state -> ", $state" } ?: ""}"
                } ?: "Not selected",
                onEditClick = onEditClick
            )

            if (uiState.clientCost.isNotEmpty()) {
                FinancialDetailsSection(
                    clientCost = uiState.clientCost,
                    negotiatedCost = uiState.negotiatedCost,
                    hideCost = uiState.hideCost,
                    onEditClick = onEditClick
                )
            }

            // Specifications Section
            SpecificationsSection(
                weight = uiState.weight.ifBlank { "0" },
                materialType = uiState.materialName,
                truckType = uiState.selectedTruckType?.displayName,
                tatRequired = uiState.tatRequired,
                tatHours = uiState.tatHours,
                tarpaulinRequired = uiState.tarpaulinRequired,
                onEditClick = onEditClick
            )

            // Remarks Section
            RemarksSection(
                remarks = uiState.remarks,
                onEditClick = onEditClick
            )
        }
    }
}
