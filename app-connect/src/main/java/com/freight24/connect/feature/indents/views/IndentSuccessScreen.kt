package com.freight24.connect.feature.indents.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.freight24.connect.feature.indents.domain.viewmodel.CloneIndentViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.CloneIndentViewModelImpl
import com.freight24.connect.feature.indents.domain.viewmodel.CreateIndentUiState
import com.freight24.connect.feature.indents.domain.viewmodel.IndentContactRole
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModelImpl
import com.freight24.common.util.DateTimeUtils
import com.freight24.connect.navigation.ConnectDestination
import com.freight24.ui.components.DocketCard
import com.freight24.ui.components.PerforationRule
import com.freight24.ui.theme.FreightThemeTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// ===========================================================================
// "Indent Created" — the lodged docket.
//
// This screen is the counterfoil: the stub torn off the docket and handed back
// to the operator as proof the indent was raised. So it is printed, not
// celebrated — a stamped seal rather than a green success circle, the indent
// code set in mono as a docket number, the lane and the cargo as ledger lines,
// and the call sheet underneath because that is the part of a lodged docket
// someone actually reads twice.
//
// It reuses the same primitives as Create / Review / Edit: DocketCard's dog-ear
// and spine, PerforationRule, LedgerLine, LaneStrip, IndentContactReadout.
// ===========================================================================

@Composable
fun IndentSuccessScreen(
    navController: NavController,
    indentId: String,
    indentCode: String? = null,
    onViewIndentClick: () -> Unit,
    onBackToHomeClick: () -> Unit,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val createIndentBackStackEntry = remember(navBackStackEntry) {
        try {
            navController.getBackStackEntry(ConnectDestination.CreateIndent.route)
        } catch (e: Exception) {
            null
        }
    }

    val cloneIndentBackStackEntry = remember(navBackStackEntry) {
        try {
            navController.getBackStackEntry(ConnectDestination.CloneIndent.route)
        } catch (e: Exception) {
            null
        }
    }

    when {
        createIndentBackStackEntry != null -> {
            val viewModel: IndentsViewModel = hiltViewModel<IndentsViewModelImpl>(createIndentBackStackEntry)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            IndentSuccessComponent(
                indentCode = indentCode ?: uiState.createdIndentCode ?: uiState.createdIndentId ?: indentId,
                weight = uiState.weight,
                material = uiState.materialName,
                fromLocation = uiState.selectedFromLocation?.name ?: "",
                toLocation = uiState.selectedToLocation?.name ?: "",
                truckType = uiState.selectedTruckType?.displayName ?: "",
                createdDate = uiState.createdDate,
                branchLabel = uiState.selectedBranch?.let { branch ->
                    listOfNotNull(
                        branch.branchName?.takeIf { it.isNotBlank() },
                        branch.branchCode?.takeIf { it.isNotBlank() },
                    ).joinToString("  ·  ").takeIf { it.isNotBlank() }
                },
                callLines = createCallLines(uiState),
                onViewIndentClick = onViewIndentClick,
                onBackToHomeClick = onBackToHomeClick
            )
        }
        cloneIndentBackStackEntry != null -> {
            val viewModel: CloneIndentViewModel = hiltViewModel<CloneIndentViewModelImpl>(cloneIndentBackStackEntry)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val indent = uiState.indent
            val groupDetails = indent?.indentGroupDetails
            val loadingLocation = groupDetails?.indentLocations?.find { it.locationType == "loading" }
            val unloadingLocation = groupDetails?.indentLocations?.find { it.locationType == "unloading" }

            IndentSuccessComponent(
                indentCode = indentCode ?: uiState.createdIndentCode ?: uiState.createdIndentId ?: indentId,
                weight = groupDetails?.loadWeight?.toString() ?: "",
                material = groupDetails?.material ?: "",
                fromLocation = loadingLocation?.areaName ?: loadingLocation?.cityName ?: "",
                toLocation = unloadingLocation?.areaName ?: unloadingLocation?.cityName ?: "",
                truckType = groupDetails?.truckType?.displayName ?: "",
                createdDate = uiState.createdDate,
                branchLabel = groupDetails?.clientBranch?.let { branch ->
                    listOfNotNull(
                        branch.branchName?.takeIf { it.isNotBlank() },
                        branch.branchCode?.takeIf { it.isNotBlank() },
                    ).joinToString("  ·  ").takeIf { it.isNotBlank() }
                },
                callLines = groupDetails?.indentContacts?.toDocketCallLines().orEmpty(),
                onViewIndentClick = onViewIndentClick,
                onBackToHomeClick = onBackToHomeClick
            )
        }
        else -> {
            IndentSuccessComponent(
                indentCode = indentCode ?: indentId,
                weight = "",
                material = "",
                fromLocation = "",
                toLocation = "",
                truckType = "",
                createdDate = null,
                onViewIndentClick = onViewIndentClick,
                onBackToHomeClick = onBackToHomeClick
            )
        }
    }
}

/** The roles the operator nominated, in roster order, for the counterfoil. */
private fun createCallLines(state: CreateIndentUiState): List<DocketCallLine> =
    IndentContactRole.entries.mapNotNull { role ->
        state.contactFor(role)?.let { contact ->
            DocketCallLine(
                roleLabel = role.label,
                name = contact.fullName?.takeIf { it.isNotBlank() } ?: "—",
                phone = contact.phone?.takeIf { it.isNotBlank() },
            )
        }
    }

@Composable
fun IndentSuccessComponent(
    indentCode: String,
    weight: String,
    material: String,
    fromLocation: String,
    toLocation: String,
    truckType: String,
    createdDate: String?,
    onViewIndentClick: () -> Unit,
    onBackToHomeClick: () -> Unit,
    branchLabel: String? = null,
    callLines: List<DocketCallLine> = emptyList(),
) {
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = FreightThemeTokens.colors
    val clipboardManager = LocalClipboardManager.current
    val hapticFeedback = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var animateSeal by remember { mutableStateOf(false) }
    val sealScale by animateFloatAsState(
        targetValue = if (animateSeal) 1f else 0.6f,
        animationSpec = tween(durationMillis = 420),
        label = "sealAnimation"
    )

    LaunchedEffect(Unit) {
        delay(120)
        animateSeal = true
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // "--" is the sentinel the fallback branch passes when it has no record.
    fun present(value: String): String? = value.takeIf { it.isNotBlank() && it != "--" }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            DocketSeal(modifier = Modifier.scale(sealScale))

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Indent Created!",
                style = MaterialTheme.typography.headlineSmall.copy(letterSpacing = (-0.4).sp),
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            DocketCard(
                accent = extendedColors.success,
                testTag = "indent_created_docket",
                onClick = null,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "INDENT CODE",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "#$indentCode",
                            style = MaterialTheme.typography.titleLarge.copy(letterSpacing = (-0.2).sp),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (createdDate != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Created ${DateTimeUtils.formatIndentTimestamp(createdDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(indentCode))
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Indent code copied",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .docketTag("indent_created_copy_code"),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy indent code",
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                PerforationRule()
                Spacer(modifier = Modifier.height(12.dp))

                LaneStrip(
                    originLabel = "Collect from",
                    originValue = present(fromLocation) ?: "—",
                    destinationLabel = "Deliver to",
                    destinationValue = present(toLocation) ?: "—",
                    originTestTag = "indent_created_origin",
                    destinationTestTag = "indent_created_destination",
                )

                Spacer(modifier = Modifier.height(12.dp))
                PerforationRule()
                Spacer(modifier = Modifier.height(8.dp))

                LedgerLine(
                    label = "Truck type",
                    value = present(truckType)?.let { titleCase(it) },
                )
                LedgerLine(
                    label = "Weight",
                    value = present(weight)?.let { "$it MT" },
                )
                LedgerLine(
                    label = "Material",
                    value = present(material)?.let { titleCase(it) },
                )

                if (branchLabel != null || callLines.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    PerforationRule()
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "CALL SHEET",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.3.sp),
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    if (branchLabel != null) {
                        LedgerLine(label = "Branch", value = branchLabel)
                    }

                    IndentContactReadout(
                        lines = callLines,
                        testTagPrefix = "indent_created_contact",
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Transporters will start quoting soon — you'll be notified.",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onViewIndentClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .docketTag("indent_created_view_details"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
            ) {
                Text(
                    text = "View Indent Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onBackToHomeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .docketTag("indent_created_back_to_dashboard"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Back to Dashboard",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

private fun titleCase(text: String): String =
    text.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

@Preview(showBackground = true, name = "Indent Created")
@Composable
fun IndentSuccessComponentPreview() {
    MaterialTheme {
        IndentSuccessComponent(
            indentCode = "IND-000118",
            weight = "5",
            material = "Fruits",
            fromLocation = "Kolar",
            toLocation = "Bengaluru",
            truckType = "22 ft",
            createdDate = "2026-09-15T17:45:00",
            branchLabel = "Kolar Depot  ·  KLR-02",
            callLines = listOf(
                DocketCallLine("Placement", "Madhan Rao", "9876543210"),
                DocketCallLine("Payment", "Ravi Deshmukh", "9876500001"),
            ),
            onViewIndentClick = {},
            onBackToHomeClick = {}
        )
    }
}
