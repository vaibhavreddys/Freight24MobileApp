package com.freight24.connect.feature.indents.views
import com.freight24.ui.components.FreightTopAppBar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import com.freight24.connect.feature.indents.domain.viewmodel.CloneIndentEvent
import com.freight24.connect.feature.indents.domain.viewmodel.CloneIndentViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.CloneIndentViewModelImpl
import com.freight24.connect.navigation.ConnectDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewCloneIndentScreen(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    navController: NavController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val cloneIndentBackStackEntry = remember(navBackStackEntry) {
        requireNotNull(navController.previousBackStackEntry) {
            "CloneIndent back stack entry not found"
        }
    }
    val viewModel: CloneIndentViewModel = hiltViewModel<CloneIndentViewModelImpl>(
        cloneIndentBackStackEntry
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
                viewModel.onEvent(CloneIndentEvent.ClearSuccess)
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CloneIndentEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = { Text("Review Clone Indent") },
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
            Button(
                onClick = { viewModel.onEvent(CloneIndentEvent.SubmitClone) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                enabled = !uiState.isSubmitting,
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp).padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                }
                Text("Clone Indent")
            }
        },
    ) { paddingValues ->
        val indent = uiState.indent
        if (indent != null) {
            val groupDetails = indent.indentGroupDetails
            val loadingLocation = groupDetails.indentLocations.find { it.locationType == "loading" }
            val unloadingLocation = groupDetails.indentLocations.find { it.locationType == "unloading" }

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
                    text = "Please review the cloned indent details before finalizing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // General Info Section
                GeneralInfoSection(
                    clientName = groupDetails.clientProfile?.companyName ?: groupDetails.clientProfile?.fullName ?: "Unknown",
                    clientPhone = groupDetails.clientProfile?.phoneNumber,
                    tagCode = groupDetails.clientProfile?.tagCode,
                )

                // Route & Financials Section
                RouteAndFinancialsSection(
                    originCity = loadingLocation?.let {
                        "${it.areaName ?: it.cityName}"
                    } ?: "Unknown",
                    destinationCity = unloadingLocation?.let {
                        "${it.areaName ?: it.cityName}"
                    } ?: "Unknown",
                    clientCost = uiState.clientCost,
                    negotiatedCost = uiState.negotiatedCost,
                    hideCost = uiState.hideCost,
                    isCostVisible = true,
                )

                // Specifications Section
                SpecificationsSection(
                    weight = groupDetails.loadWeight.toString(),
                    weightUnit = "MT",
                    materialType = groupDetails.material,
                    truckType = groupDetails.truckType?.displayName,
                    truckTypeUnit = groupDetails.truckType?.bodyType ?: "",
                    tatRequired = uiState.tatRequired,
                    tatHours = uiState.tatHours,
                    tarpaulinRequired = uiState.tarpaulinRequired,
                )

                // Remarks Section
                if (uiState.remarks.isNotBlank()) {
                    RemarksSection(
                        remarks = uiState.remarks,
                    )
                }
            }
        }
    }
}

//@Composable
//fun CloneSettingsSection(
//    assignedTo: String,
//    notes: String,
//    onEditClick: () -> Unit
//) {
//    ReviewSection(title = "Clone Settings", onEditClick = onEditClick) {
//        if (assignedTo.isNotBlank()) {
//            ReviewItem(label = "Assigned To", value = assignedTo)
//        }
//        if (notes.isNotBlank()) {
//            ReviewItem(label = "Notes", value = notes)
//        }
//    }
//}
