package com.freight24.connect.feature.trips.views
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.trips.domain.viewmodel.CreateTripEvent
import com.freight24.connect.feature.trips.domain.viewmodel.CreateTripUiState
import com.freight24.connect.feature.trips.domain.viewmodel.CreateTripViewModel
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.formViewportInsets
import com.freight24.ui.theme.FreightThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateTripScreen(
    indentId: String,
    truckId: String,
    onBackClick: () -> Unit,
    onTripCreated: (String, String, String, String, String, String) -> Unit,
    viewModel: CreateTripViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val colorScheme = colorScheme
    val extendedColors = com.freight24.ui.theme.FreightThemeTokens.colors

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CreateTripEvent.ClearSnackbar)
        }
    }

    LaunchedEffect(indentId, truckId) {
        viewModel.onEvent(CreateTripEvent.LoadData(indentId, truckId))
    }

    LaunchedEffect(uiState.createdTrip) {
        if (uiState.createdTrip != null) {
            val trip = uiState.createdTrip!!.trip
            val truckNumber = uiState.truck?.registrationNumber ?: ""
            val groupDetails = uiState.indent?.indentGroupDetails
            val loadingLocation = groupDetails?.indentLocations?.find { it.locationType == "loading" }
            val unloadingLocation = groupDetails?.indentLocations?.find { it.locationType == "unloading" }
            val origin = loadingLocation?.areaName ?: loadingLocation?.cityName ?: ""
            val destination = unloadingLocation?.areaName ?: unloadingLocation?.cityName ?: ""
            val driverPhone = uiState.driverPhone
            val driverName = uiState.driverName

            onTripCreated(trip.tripCode, truckNumber, origin, destination, driverName, driverPhone)
        }
    }

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = { Text("Create Trip", fontWeight = FontWeight.SemiBold) },
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
                onClick = { viewModel.onEvent(CreateTripEvent.CreateTrip) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp, top = 10.dp)
                        .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary,
                    ),
                enabled = !uiState.isCreating && !uiState.isLoading,
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).padding(end = 8.dp),
                        color = colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                }
                Text("CREATE TRIP", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    // Keyboard-aware viewport; see FormInsets.kt.
                    .formViewportInsets(paddingValues),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.errorMessage != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = uiState.errorMessage!!, color = colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.onEvent(CreateTripEvent.LoadData(indentId, truckId)) }) {
                        Text("Retry")
                    }
                }
            } else {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Indent Info Card
                    IndentInfoCard(uiState)

                    // Selected Truck Card
                    SelectedTruckCard(uiState)

                    // Driver & Schedule Card
                    DriverDetailsCard(uiState, viewModel)
                }
            }
        }
    }
}

@Composable
fun IndentInfoCard(uiState: CreateTripUiState) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        val extendedColors = FreightThemeTokens.colors
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Route, // Or similar info icon
                    contentDescription = "Info",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "ROUTE DETAILS",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Origin Destination Visual
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(20.dp),
                ) {
                    // Origin Dot
                    Box(
                        modifier =
                            Modifier
                                .size(10.dp)
                                .background(colorScheme.primary, androidx.compose.foundation.shape.CircleShape),
                    )
                    // Dotted Line
                    val dottedLineColor = colorScheme.surfaceVariant
                    Canvas(
                        modifier =
                            Modifier
                                .width(2.dp)
                                .weight(1f)
                                .padding(vertical = 4.dp),
                    ) {
                        drawLine(
                            color = dottedLineColor,
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                            strokeWidth = 4f,
                        )
                    }
                    // Destination Dot
                    Box(
                        modifier =
                            Modifier
                                .size(10.dp)
                                .background(colorScheme.secondary, androidx.compose.foundation.shape.CircleShape),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
                    val groupDetails = uiState.indent?.indentGroupDetails
                    val loadingLocation = groupDetails?.indentLocations?.find { it.locationType == "loading" }
                    val unloadingLocation = groupDetails?.indentLocations?.find { it.locationType == "unloading" }

                    Column {
                        Text(
                            "ORIGIN",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Text(
                            loadingLocation?.areaName ?: loadingLocation?.cityName ?: "Unknown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Text(
                            "DESTINATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Text(
                            unloadingLocation?.areaName ?: unloadingLocation?.cityName ?: "Unknown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = colorScheme.outlineVariant)

            // Details Grid
            val groupDetails = uiState.indent?.indentGroupDetails
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("CLIENT", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                    Text(
                        groupDetails?.clientProfile?.companyName ?: groupDetails?.clientProfile?.fullName ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("CLIENT COST", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                    Text(
                        "₹${formatCurrency(groupDetails?.clientTripCost)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("MATERIAL TYPE", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                    Text(
                        groupDetails?.material ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("NEGOTIATED COST", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                    Text(
                        "₹${formatCurrency(uiState.indent?.negotiatedTripCost)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.success,
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedTruckCard(uiState: CreateTripUiState) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        val extendedColors = FreightThemeTokens.colors
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocalShipping,
                    contentDescription = "Truck",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "SELECTED TRUCK",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = extendedColors.success.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "TRUCK NUMBER",
                            style = MaterialTheme.typography.labelSmall,
                            color = extendedColors.success,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            uiState.truck?.registrationNumber ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                        )
                    }
                    Surface(
                        color = extendedColors.success,
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            uiState.truck?.displayName ?: uiState.truck?.bodyType ?: "Truck", // Using truckType or displayName if available
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DriverDetailsCard(
    uiState: CreateTripUiState,
    viewModel: CreateTripViewModel,
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        val isPhoneValid = uiState.driverPhone.matches(Regex("^\\+?[0-9]{10,15}$"))

        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Driver",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "DRIVER DETAILS",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Driver Phone
            Text(
                "Driver Phone",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.driverPhone,
                onValueChange = { newValue ->
                    // Keep only digits and an optional leading '+' so IME-inserted
                    // spaces/separators can never break the E.164-style validation.
                    val filtered =
                        if (newValue.startsWith("+")) {
                            "+" + newValue.drop(1).filter { it.isDigit() }
                        } else {
                            newValue.filter { it.isDigit() }
                        }

                    // Limit to 15 chars (E.164 international standard max length)
                    val limited = filtered.take(15)

                    // Update state only if value actually changed
                    if (limited != uiState.driverPhone) {
                        viewModel.onEvent(CreateTripEvent.UpdateDriverPhone(limited))
                    }
                },
                isError = uiState.driverPhone.isNotBlank() && !isPhoneValid,
                supportingText = {
                    if (!isPhoneValid && uiState.driverPhone.isNotBlank()) {
                        Text("Enter a valid phone number", color = colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                placeholder = { Text("Enter phone number", color = colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = colorScheme.surfaceVariant,
                        focusedContainerColor = colorScheme.surface,
                        unfocusedBorderColor = colorScheme.outlineVariant,
                    ),
                shape = RoundedCornerShape(8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Driver Name
            Text(
                "Driver Name",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.driverName,
                onValueChange = { viewModel.onEvent(CreateTripEvent.UpdateDriverName(it)) },
                placeholder = { Text("Enter driver name", color = colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = colorScheme.surfaceVariant,
                        focusedContainerColor = colorScheme.surface,
                        unfocusedBorderColor = colorScheme.outlineVariant,
                    ),
                shape = RoundedCornerShape(8.dp),
            )
        }
    }
}

private fun formatCurrency(amount: Double?): String {
    if (amount == null) return "0"
    return "%,.0f".format(amount)
}
