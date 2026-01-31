package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.freight24.connect.feature.indents.domain.viewmodel.IndentDetailsEvent
import com.freight24.connect.feature.indents.domain.viewmodel.IndentDetailsViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.IndentDetailsViewModelImpl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndentDetailsScreen(
    navController: NavController,
    indentId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    viewModel: IndentDetailsViewModel = hiltViewModel<IndentDetailsViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val refreshTrigger = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refresh", false)
        ?.collectAsStateWithLifecycle()

    LaunchedEffect(indentId, refreshTrigger) {
        if (refreshTrigger?.value == true) {
            viewModel.onEvent(IndentDetailsEvent.Refresh)
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
        } else {
            viewModel.onEvent(IndentDetailsEvent.LoadIndent(indentId))
        }
    }

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
        },
        bottomBar = {
            Button(
                onClick = {

                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                enabled = true,
            ) {
//                if (uiState.isSubmitting) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(20.dp).padding(end = 8.dp),
//                        color = Color.White,
//                        strokeWidth = 2.dp,
//                    )
//                }
                uiState.indent?.let { indent ->
                    if(indent.status.equals("open", true)) {
                        Text("Ready for Assignment")
                    } else if (indent.status.equals("READY_FOR_ASSIGNMENT", true))  {
                        Text("Create trip")
                    }
                }

            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = uiState.errorMessage ?: "Unknown Error", color = Color.Red)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.onEvent(IndentDetailsEvent.Refresh) }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.indent != null -> {
                    val indent = uiState.indent!!
                    val groupDetails = indent.indentGroupDetails
                    val loadingLocation = groupDetails.indentLocations.find { it.locationType == "loading" }
                    val unloadingLocation = groupDetails.indentLocations.find { it.locationType == "unloading" }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GeneralInfoSection(
                            clientName = groupDetails.clientProfile?.companyName ?: groupDetails.clientProfile?.fullName ?: "Unknown",
                            tagCode = groupDetails.clientProfile?.tagCode.orEmpty(), // tagCode not available in IndentResponse
                            onEditClick = null
                        )

                        RouteDetailsSection(
                            originCity = loadingLocation?.let {
                                "${it.areaName ?: it.cityName}${it.address?.let { addr -> ", $addr" } ?: ""}"
                            } ?: "Unknown",
                            destinationCity = unloadingLocation?.let {
                                "${it.areaName ?: it.cityName}${it.address?.let { addr -> ", $addr" } ?: ""}"
                            } ?: "Unknown",
                            onEditClick = null
                        )

                        FinancialDetailsSection(
                            clientCost = formatCurrencyDetails(groupDetails.clientTripCost),
                            negotiatedCost = formatCurrencyDetails(indent.negotiatedTripCost),
                            hideCost = indent.hideCostInfo,
                            onEditClick = {
                                onEditClick(indentId)
                            }
                        )

                        SpecificationsSection(
                            weight = groupDetails.loadWeight.toString(),
                            materialType = groupDetails.material,
                            truckType = groupDetails.truckType?.displayName,
                            tatRequired = groupDetails.tatEnabled,
                            tatHours = groupDetails.tatHours?.toString() ?: "",
                            tarpaulinRequired = groupDetails.tarpaulingRequired,
                            onEditClick = {
                                onEditClick(indentId)
                            }
                        )

                        RemarksSection(
                            remarks = groupDetails.specialRequirements ?: "",
                            onEditClick = null
                        )
                    }
                }
            }
        }
    }
}

private fun formatCurrencyDetails(amount: Double?): String {
    if (amount == null) return "0"
    return "%,.0f".format(amount)
}
