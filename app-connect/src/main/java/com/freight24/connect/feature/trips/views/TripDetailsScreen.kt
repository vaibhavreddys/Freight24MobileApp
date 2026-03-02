package com.freight24.connect.feature.trips.views

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.rememberAsyncImagePainter
import com.freight24.connect.feature.trips.domain.viewmodel.TripDetailsEvent
import com.freight24.connect.feature.trips.domain.viewmodel.TripDetailsViewModel
import com.freight24.connect.feature.trips.domain.viewmodel.TripDetailsViewModelImpl
import com.freight24.connect.feature.trips.views.components.TripCard
import com.freight24.connect.feature.trips.views.components.formatAmount
import com.freight24.connect.utils.toDateTime
import com.freight24.rest.response.TripDataResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    navController: androidx.navigation.NavController,
    tripId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onDocumentView : (String, String, String) -> Unit,
    onHaltingClick: (String, String, Int, Int, String?) -> Unit,
    onLuClick: (String, String, String?, String?, String?, String?, String?) -> Unit,
    onOtherDeductionsClick: (String, String?, String?) -> Unit,
    viewModel: TripDetailsViewModel = hiltViewModel<TripDetailsViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showSelectionDialog by remember { mutableStateOf(false) }
    var pendingStatus by remember { mutableStateOf("") }
    var pendingButtonText by remember { mutableStateOf("") }
    
    // Refresh trip details when returning from PDF viewer via navigation result
    val refreshTrigger = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refresh", false)
        ?.collectAsStateWithLifecycle()

    LaunchedEffect(tripId, refreshTrigger?.value) {
        if (refreshTrigger?.value == true) {
            viewModel.onEvent(TripDetailsEvent.Refresh)
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
        }
    }
    
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                viewModel.onEvent(TripDetailsEvent.OnFilesPicked(uris, isPdf = false))
                showSheet = true
            }
        }
    )

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.onEvent(TripDetailsEvent.OnFilesPicked(listOf(it), isPdf = true))
                showSheet = true
            }
        }
    )

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Confirm Status Update") },
            text = { Text("Are you sure you want to update the trip status to ${pendingButtonText.replace("Update to ", "")}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(TripDetailsEvent.UpdateStatus(newStatus = pendingStatus))
                        showConfirmationDialog = false
                    }
                ) {
                    Text("Confirm", color = Color(0xFF10B981))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showSelectionDialog = false },
            title = { Text("Select Document Type") },
            text = { Text("Select images from gallery or a PDF file to upload.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        showSelectionDialog = false
                    }
                ) {
                    Text("Images", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pdfPickerLauncher.launch("application/pdf")
                        showSelectionDialog = false
                    }
                ) {
                    Text("PDF", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showSheet && uiState.selectedUris.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { 
                showSheet = false
                viewModel.onEvent(TripDetailsEvent.OnClearSelection)
            },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (uiState.isPdf) "PDF Selected" else "Images Selected (${uiState.selectedUris.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (uiState.isPdf) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Red
                    )
                    Text(text = "PDF Document", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.selectedUris) { uri ->
                            Box {
                                Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        viewModel.onEvent(TripDetailsEvent.UploadLR)
                        showSheet = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    enabled = !uiState.isUploading
                ) {
                    if (uiState.isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("Upload", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip Details", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { onEditClick(tripId) }) {
                        Text("Edit", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            val trip = uiState.trip
            val truckerStatus = trip?.truckerStatus ?: ""
            val (buttonText, nextStatus) = when (truckerStatus.uppercase()) {
                "ASSIGNED" -> "Update to LOADING" to "LOADING"
                "LOADING" -> "Upload LR" to "UPLOAD_LR"
                "IN_TRANSIT" -> "Update to UNLOADED" to "UNLOADED"
                else -> null to null
            }

            if (buttonText != null && nextStatus != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Button(
                        onClick = {
                            if (nextStatus == "UPLOAD_LR") {
                                showSelectionDialog = true
                            } else {
                                pendingStatus = nextStatus
                                pendingButtonText = buttonText
                                showConfirmationDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (nextStatus == "UPLOAD_LR") MaterialTheme.colorScheme.primary else Color(0xFF10B981)
                        )
                    ) {
                        Text(buttonText, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF1F5F9))
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.trip != null -> {
                    TripDetailsContent(
                        trip = uiState.trip!!,
                        onUploadLrClick = { showSelectionDialog = true },
                        onDocClick = { docId, status ->
                            onDocumentView(tripId, docId, status)
                        },
                        onHaltingClick = { tripId, truckTypeId, loadingDays, unloadingDays, notes ->
                            onHaltingClick(tripId, truckTypeId, loadingDays, unloadingDays, notes)
                        },
                        onLuClick = { loading, unloading, loadingSlipUrl, unloadingSlipUrl, loadingId, unloadingId, notes ->
                            onLuClick(loading, unloading, loadingSlipUrl, unloadingSlipUrl, loadingId, unloadingId, notes)
                        },
                        onOtherDeductionsClick = onOtherDeductionsClick
                    )
                }
            }
        }
    }
}

@Composable
fun TripDetailsContent(
    trip: TripDataResponse,
    onUploadLrClick: () -> Unit,
    onDocClick: (String, String) -> Unit,
    onHaltingClick: (String, String, Int, Int, String?) -> Unit,
    onLuClick: (String, String, String?, String?, String?, String?, String?) -> Unit,
    onOtherDeductionsClick: (String, String?, String?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Reused Trip Card
        TripCard(trip = trip)


        // Trip Documents
        ExpandableSection(
            title = "Documents",
            icon = Icons.Outlined.Description,
            iconBg = Color(0xFFEFF6FF),
            iconTint = Color(0xFF3B82F6)
        ) {
            TripDocumentsSection(
                truckerStatus = trip.truckerStatus,
                documents = trip.documents ?: emptyList(),
                onUploadLrClick = onUploadLrClick,
                onDocClick = onDocClick
            )
        }

        // Charges & Deductions
        ExpandableSection(
            title = "Charges & Deductions",
            icon = Icons.Outlined.AccountBalanceWallet,
            iconBg = Color(0xFFECFDF5),
            iconTint = Color(0xFF10B981)
        ) {
            ChargesAndDeductionsContent(
                financials = trip.financials,
                onHaltingClick = {
                    val truckTypeId = trip.indentDetails.truckTypeId
                    val loadingDays = trip.financials?.halting?.loading?.days ?: 0
                    val unloadingDays = trip.financials?.halting?.unloading?.days ?: 0
                    val notes = trip.financials?.halting?.loading?.notes
                    onHaltingClick(trip.id, truckTypeId, loadingDays, unloadingDays, notes)
                },
                onLuClick = onLuClick,
                onOtherDeductionsClick = { amount, notes ->
                    onOtherDeductionsClick(trip.id, amount.toInt().toString(), notes)
                }
            )
        }

        // Payment Breakdown
        ExpandableSection(
            title = "Payment Breakdown",
            icon = Icons.Outlined.Payments,
            iconBg = Color(0xFFECFDF5),
            iconTint = Color(0xFF10B981)
        ) {
            PaymentBreakdownContent(trip.financials)
        }

        // Trip Timeline
        ExpandableSection(
            title = "Timeline",
            icon = Icons.Outlined.History
        ) {
            TripTimelineContent(timeline = trip.timelines ?: emptyList())
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}


@Composable
fun TripDocumentsSection(
    truckerStatus: String,
    documents: List<TripDataResponse.Document>,
    onUploadLrClick: () -> Unit,
    onDocClick: (String, String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        val lrDoc = documents.find { it.type.lowercase() == "lr" }
        val memoDoc = documents.find { it.type.lowercase() == "loading_memo" }

        DocumentCard(
            title = "Lorry Receipt (LR)",
            doc = lrDoc,
            showUploadButton = true,
            isUploadEnabled = truckerStatus == "LOADING" || (lrDoc != null && (lrDoc.status.lowercase() == "pending" || lrDoc.status.lowercase() == "rejected")),
            onUploadClick = onUploadLrClick,
            onCardClick = { docId, status -> onDocClick(docId, status) }
        )

        DocumentCard(
            title = "Trip Memo",
            doc = memoDoc,
            showUploadButton = false
        )
    }
}

@Composable
fun DocumentCard(
    title: String,
    doc: TripDataResponse.Document?,
    showUploadButton: Boolean = false,
    isUploadEnabled: Boolean = false,
    onUploadClick: () -> Unit = {},
    onCardClick: (String, String) -> Unit = { _, _ -> }
) {

    fun onCardClick() {
        doc?.let {
            val status = it.status.lowercase()
            if (status == "verified" || status == "rejected" || status == "pending") {
                onCardClick(it.id, it.status)
            }
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            onCardClick()
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFEFF6FF),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                
                if (doc == null) {
                    Text(
                        text = "Not Available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFEF4444)
                    )
                }
            }

            val docStatus = doc?.status?.lowercase()
            val isPending = docStatus == "pending"
            val isRejected = docStatus == "rejected"
            val showUpload = showUploadButton && (doc == null || isPending || isRejected)

            if (showUpload) {
                val buttonText = if (isPending) "Verify" else "Upload LR"
                val handleButtonClick = if (isPending) { { onCardClick() } } else onUploadClick

                Button(
                    onClick = handleButtonClick,
                    enabled = isUploadEnabled,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFECFDF5),
                        contentColor = Color(0xFF10B981),
                        disabledContainerColor = Color(0xFFECFDF5).copy(alpha = 0.5f),
                        disabledContentColor = Color(0xFF10B981).copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (doc != null) {
                StatusBadgeSmall(status = doc.status)
            }
        }
    }
}

@Composable
fun StatusBadgeSmall(status: String) {
    val (color, ticketColor) = when (status.lowercase()) {
        "verified", "approved" -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        "pending" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        "rejected" -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
        else -> Color(0xFFF3F4FB) to Color.Gray
    }

    Surface(
        color = color,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ticketColor, fontSize = 10.sp)
        )
    }
}

@Composable
fun ExpandableSection(
    title: String,
    icon: ImageVector,
    iconBg: Color = Color(0xFFF1F5F9),
    iconTint: Color = Color(0xFF3B82F6),
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = iconBg,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = iconTint)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
            
            AnimatedVisibility(visible = expanded) {
                content()
            }
        }
    }
}

@Composable
fun ChargesAndDeductionsContent(
    financials: TripDataResponse.FinanceDetails?,
    onHaltingClick: () -> Unit,
    onLuClick: (String, String, String?, String?, String?, String?, String?) -> Unit,
    onOtherDeductionsClick: (Double, String?) -> Unit
) {
    if (financials == null) {
        Text("No data", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
        return
    }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val luTotal = (financials.luCharges.loading.total + financials.luCharges.unloading.total).toDouble()
        val haltingTotal = (financials.halting.loading.totalAmount + financials.halting.unloading.totalAmount)
        
        ChargesSubRow("Loading & Unloading", luTotal, onClick = {
            onLuClick(
                financials.luCharges.loading.total.toString(),
                financials.luCharges.unloading.total.toString(),
                financials.luCharges.loading.slipUrl,
                financials.luCharges.unloading.slipUrl,
                financials.luCharges.loading.id,
                financials.luCharges.unloading.id,
                financials.luCharges.loading.notes,
            )
        })
        ChargesSubRow("Halting", haltingTotal, onClick = onHaltingClick)
        val otherDeductionsNotes = financials.deductionRemarks
        ChargesSubRow("Other Deductions", financials.otherDeductions, isDeduction = true, onClick = {
            onOtherDeductionsClick(financials.otherDeductions, otherDeductionsNotes)
        })
    }
}

@Composable
fun ChargesSubRow(
    label: String, 
    amount: Double, 
    isDeduction: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label, 
            style = MaterialTheme.typography.bodyMedium, 
            color = Color(0xFF475569)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = (if (isDeduction) "- " else "") + "₹${formatAmount(amount.toString())}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDeduction) Color.Red else Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color(0xFF3B82F6)
            )
        }
    }
}

@Composable
fun PaymentBreakdownContent(financials: TripDataResponse.FinanceDetails?) {
    if (financials == null) {
        Text("Financial data not available", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
        return
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        PaymentDetailRow("Trip Cost", financials.truckerTripCost)
        
        // Halting Loading
        PaymentDetailRow(
            label = "Loading",
            amount = financials.halting.loading.totalAmount,
            isAddition = true,
            detailText = "(${financials.halting.loading.days} Days x ₹${formatAmount(financials.halting.loading.perDayAmount.toString())})",
            addHeader = true,
            headerName = "Halting Charges"
        )

        // Halting Unloading
        PaymentDetailRow(
            label = "Unloading",
            amount = financials.halting.unloading.totalAmount,
            isAddition = true,
            detailText = "(${financials.halting.unloading.days} Days x ₹${formatAmount(financials.halting.unloading.perDayAmount.toString())})"
        )

        // Loading Charges (from LU)
        PaymentDetailRow(
            label = "Loading Charges",
            amount = financials.luCharges.loading.total.toDouble(),
            isAddition = true
        )

        // Unloading Charges (from LU)
        PaymentDetailRow(
            label = "Unloading Charges",
            amount = financials.luCharges.unloading.total.toDouble(),
            isAddition = true
        )

        PaymentDetailRow("Other Deductions", financials.otherDeductions, isDeduction = true)
        PaymentDetailRow("Advance Paid", financials.advance.paidAmount)
        PaymentDetailRow("Platform Fee", financials.platformFee.amount, isDeduction = true)
        PaymentDetailRow("TDS", financials.tds.amount, isDeduction = true)
        
        Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Net Payable", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text("₹${formatAmount(financials.netPayable.toString())}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Paid Amount", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text("₹${formatAmount(financials.totalPaidAmount.toString())}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Balance Due Box
        val balanceDue = financials.netPayable - financials.totalPaidAmount
        Surface(
            color = Color(0xFFFFF7ED),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Balance Due", 
                    fontWeight = FontWeight.Bold, 
                    color = Color(0xFF7C2D12),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "₹${formatAmount(balanceDue.toString())}", 
                    fontWeight = FontWeight.Bold, 
                    color = Color(0xFFEA580C),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun PaymentDetailRow(
    label: String,
    amount: Double,
    isDeduction: Boolean = false,
    isAddition: Boolean = false,
    detailText: String? = null,
    addHeader: Boolean = false,
    headerName: String = ""
) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        if (addHeader) {
            Text(
                text = headerName, 
                style = MaterialTheme.typography.bodySmall, 
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = Color(0xFF64748B)
                )
                if (!detailText.isNullOrBlank()) {
                    Text(
                        text = detailText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
            Text(
                text = when {
                    isDeduction -> "- ₹${formatAmount(amount.toString())}"
                    isAddition && amount > 0 -> "+ ₹${formatAmount(amount.toString())}"
                    else -> "₹${formatAmount(amount.toString())}"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    isDeduction -> Color.Red
                    isAddition && amount > 0 -> Color(0xFF10B981)
                    else -> Color(0xFF1E293B)
                }
            )
        }
    }
}

@Composable
fun TripTimelineContent(timeline: List<TripDataResponse.Timeline>) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        timeline.forEachIndexed { index, item ->
            TimelineItemRow(
                item = item,
                isLast = index == timeline.size - 1
            )
        }
    }
}

@Composable
fun TimelineItemRow(
    item: TripDataResponse.Timeline,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = if (item.isCompleted) Color(0xFF10B981) else Color(0xFFE2E8F0),
                        shape = CircleShape
                    )
                    .then(
                        if (item.isCurrent && !item.isCompleted) {
                            Modifier.background(Color.White, CircleShape)
                                .then(Modifier.size(12.dp).background(Color(0xFF10B981), CircleShape))
                        } else Modifier
                    )
            )
            
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(
                            if (item.isCompleted) Color(0xFF10B981) else Color(0xFFE2E8F0)
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.padding(bottom = if (isLast) 16.dp else 24.dp)) {
            Text(
                text = mapStatusToTitle(item.statusType),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (item.isCompleted || item.isCurrent) Color(0xFF1E293B) else Color(0xFF94A3B8)
            )
            
            val subText = if (item.isCurrent && !item.isCompleted) {
                "In Progress"
            } else {
                item.updatedAt?.let { 
                    try {
                        it.toDateTime()
                    } catch (e: Exception) {
                        it
                    }
                } ?: ""
            }

            if (subText.isNotEmpty()) {
                Text(
                    text = subText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.isCurrent && !item.isCompleted) Color(0xFF10B981) else Color(0xFF64748B)
                )
            }
        }
    }
}

fun mapStatusToTitle(statusType: String): String {
    val normalized = statusType.uppercase().replace("-", "_")
    return when (normalized) {
        "ASSIGNED" -> "Trip Confirmed"
        "LOADING" -> "Loading"
        "IN_TRANSIT" -> "In Transit"
        "UNLOADED" -> "Unloaded"
        "INVOICED" -> "Invoiced"
        "POD_DISPATCHED" -> "POD Dispatched"
        "PAID" -> "Paid"
        "CLOSED" -> "Closed"
        else -> normalized.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }
}
