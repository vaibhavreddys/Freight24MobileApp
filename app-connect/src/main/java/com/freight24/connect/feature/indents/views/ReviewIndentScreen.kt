package com.freight24.connect.feature.indents.views

import com.freight24.ui.components.FreightTopAppBar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
                    ConnectDestination.IndentSuccess.createRoute(indentId, uiState.createdIndentCode)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Please review details before finalizing.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            GeneralInfoSection(
                clientName = uiState.selectedClient?.companyName?.takeIf { it.isNotBlank() }
                    ?: "Not selected",
                clientPhone = uiState.selectedClient?.phoneNumber,
                tagCode = uiState.selectedClient?.tagCode,
                onEditClick = onEditClick,
            )

            RouteAndFinancialsSection(
                originCity = uiState.selectedFromLocation?.let {
                    "${it.name}${it.state?.let { s -> ", $s" } ?: ""}"
                } ?: "Not selected",
                destinationCity = uiState.selectedToLocation?.let {
                    "${it.name}${it.state?.let { s -> ", $s" } ?: ""}"
                } ?: "Not selected",
                clientCost = uiState.clientCost,
                negotiatedCost = uiState.negotiatedCost,
                hideCost = uiState.hideCost,
                onEditClick = onEditClick,
            )

            SpecificationsSection(
                weight = uiState.weight.ifBlank { "0" },
                materialType = uiState.materialName,
                truckType = uiState.selectedTruckType?.displayName,
                tatRequired = uiState.tatRequired,
                tatHours = uiState.tatHours,
                tarpaulinRequired = uiState.tarpaulinRequired,
                onEditClick = onEditClick,
            )

            if (uiState.remarks.isNotEmpty()) {
                RemarksSection(
                    remarks = uiState.remarks,
                    onEditClick = onEditClick,
                )
            }
        }
    }
}
