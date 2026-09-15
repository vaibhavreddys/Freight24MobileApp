package com.freight24.connect.feature.indents.views

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.freight24.connect.feature.indents.domain.viewmodel.CreateIndentEvent
import com.freight24.connect.feature.indents.domain.viewmodel.CreateIndentUiState
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModelImpl
import com.freight24.connect.navigation.ConnectDestination
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.MetaTag
import com.freight24.ui.components.PerforationRule

// ===========================================================================
// Review Indent — the completed docket, read back before it is raised.
//
// Same six clauses in the same order as the create screen, so the operator
// reads the document they just filled in rather than a differently-shaped
// summary of it. Nothing is collapsed and nothing is summarised away: this is
// the last chance to catch a wrong rate before the indent reaches the load
// board, so every field the form collected is printed.
// ===========================================================================

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
        createIndentBackStackEntry,
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            uiState.createdIndentId?.let { indentId ->
                navController.navigate(
                    ConnectDestination.IndentSuccess.createRoute(indentId, uiState.createdIndentCode),
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FreightTopAppBar(
                title = { Text("Review Indent") },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.docketTag("review_indent_back"),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onEditClick,
                        modifier = Modifier.docketTag("review_indent_edit"),
                    ) {
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
                    .padding(bottom = 20.dp, top = 10.dp),
            ) {
                Button(
                    onClick = { viewModel.onEvent(CreateIndentEvent.SubmitIndent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .docketTag("review_indent_confirm_button"),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    enabled = !uiState.isSubmitting,
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 0.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(10.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        if (uiState.isSubmitting) "Raising indent…" else "Confirm & Create Indent",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ReviewHead()

            ClauseDocket(
                number = 1,
                title = "Consignor",
                state = consignorClauseState(uiState, emptySet()),
                testTag = "review_indent_clause_consignor",
            ) {
                LedgerRow(
                    label = "Client",
                    value = uiState.selectedClient?.let { client ->
                        listOfNotNull(
                            client.companyName.takeIf { it.isNotBlank() },
                            client.tagCode.takeIf { it.isNotBlank() },
                        ).joinToString(" · ")
                    },
                )
                LedgerDivider()
                LedgerRow(
                    label = "Client phone",
                    value = uiState.selectedClient?.phoneNumber,
                )
                LedgerDivider()
                LedgerRow(
                    label = "Branch",
                    value = uiState.selectedBranch?.let { branch ->
                        listOfNotNull(
                            branch.branchName?.takeIf { it.isNotBlank() },
                            branch.branchCode?.takeIf { it.isNotBlank() },
                        ).joinToString(" · ")
                    },
                )
                Spacer(Modifier.height(14.dp))
                PerforationRule()
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "CALL SHEET",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(6.dp))
                CallSheetReadout(contactFor = uiState::contactFor)
            }

            ClauseDocket(
                number = 2,
                title = "Lane",
                state = laneClauseState(uiState, emptySet()),
                testTag = "review_indent_clause_lane",
            ) {
                LaneReadout(
                    originLabel = "Loading from",
                    originValue = uiState.selectedFromLocation?.name.orEmpty(),
                    destinationLabel = "Unloading at",
                    destinationValue = uiState.selectedToLocation?.name.orEmpty(),
                    originTestTag = "review_indent_from",
                    destinationTestTag = "review_indent_to",
                )
            }

            ClauseDocket(
                number = 3,
                title = "Vehicle",
                state = vehicleClauseState(uiState, emptySet()),
                testTag = "review_indent_clause_vehicle",
            ) {
                LedgerRow(
                    label = "Vehicle type",
                    value = uiState.selectedTruckType?.displayName,
                )
                LedgerDivider()
                LedgerRow(
                    label = "Body type",
                    value = uiState.selectedTruckType?.bodyType,
                )
            }

            ClauseDocket(
                number = 4,
                title = "Cargo",
                state = cargoClauseState(uiState, emptySet()),
                testTag = "review_indent_clause_cargo",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LedgerRow(
                        label = "Weight",
                        value = uiState.weight.takeIf { it.isNotBlank() }?.let { "$it MT" },
                        modifier = Modifier.weight(1f),
                    )
                    LedgerRow(
                        label = "Material",
                        value = uiState.materialName,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            ClauseDocket(
                number = 5,
                title = "Commercials",
                state = commercialsClauseState(uiState, emptySet()),
                testTag = "review_indent_clause_commercials",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LedgerRow(
                        label = "Client cost",
                        value = uiState.clientCost.takeIf { it.isNotBlank() }?.let { "₹$it" },
                        modifier = Modifier.weight(1f),
                    )
                    LedgerRow(
                        label = "Proposed cost",
                        value = uiState.businessProposedCost.takeIf { it.isNotBlank() }
                            ?.let { "₹$it" },
                        modifier = Modifier.weight(1f),
                    )
                }
                LedgerDivider()
                LedgerRow(
                    label = "Negotiated trip cost",
                    value = uiState.negotiatedCost.takeIf { it.isNotBlank() }?.let { "₹$it" },
                )
                LedgerDivider()
                LedgerRow(
                    label = "Cost visibility",
                    value = if (uiState.hideCost) "Hidden from load board" else "Shown to truck owners",
                )
            }

            ClauseDocket(
                number = 6,
                title = "Terms",
                state = termsClauseState(uiState, emptySet()),
                testTag = "review_indent_clause_terms",
            ) {
                LedgerRow(
                    label = "Turnaround time",
                    value = if (uiState.tatRequired) {
                        "${uiState.tatHours.ifBlank { "0" }} hours"
                    } else {
                        "Not committed"
                    },
                )
                LedgerDivider()
                LedgerRow(
                    label = "Tarpaulin",
                    value = if (uiState.tarpaulinRequired) "Required" else "Not required",
                )
                LedgerDivider()
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LedgerRow(
                        label = "Vehicle height",
                        value = uiState.vehicleHeight.takeIf { it.isNotBlank() }?.let { "$it ft" },
                        modifier = Modifier.weight(1f),
                    )
                    LedgerRow(
                        label = "Flooring",
                        value = uiState.flooringType,
                        modifier = Modifier.weight(1f),
                    )
                }
                LedgerDivider()
                LedgerRow(
                    label = "Remarks",
                    value = uiState.remarks.takeIf { it.isNotBlank() },
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReviewHead() {
    val scheme = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "READY TO RAISE",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                fontWeight = FontWeight.Bold,
                color = scheme.primary,
            )
            Spacer(Modifier.weight(1f))
            MetaTag(
                text = "DRAFT",
                container = scheme.surfaceContainerHighest,
                content = scheme.onSurfaceVariant,
            )
        }
        Text(
            text = "Consignment note",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
        )
        Text(
            text = "Check the lane and the rates. Once raised, this goes to the load board " +
                "and truck owners start expressing interest.",
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
    }
}
