package com.freight24.connect.feature.truckowners.views

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.connect.feature.truckowners.domain.viewmodel.OnboardTruckOwnerViewModel
import com.freight24.connect.feature.truckowners.profile.domain.DocumentStatus
import com.freight24.connect.feature.truckowners.profile.domain.DocumentType
import com.freight24.connect.feature.truckowners.profile.util.ProfileFieldValidators
import com.freight24.connect.feature.truckowners.profile.util.ValidationResult
import com.freight24.ui.components.AddRouteBottomSheet
import com.freight24.ui.components.CitySearchField
import com.freight24.ui.components.RouteChip
import com.freight24.ui.theme.FreightThemeTokens
import com.freight24.ui.theme.PrimaryGreen

private val BrandGreen = PrimaryGreen

private enum class InputValidationType {
    NONE,
    AADHAAR,
    PAN,
    GST,
    IFSC,
    UPI,
    PHONE,
    EMAIL,
    ACCOUNT_NUMBER,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardTruckOwnerScreen(
    onBackClick: () -> Unit,
    onCloseSuccess: () -> Unit,
    onViewTruckOwnerProfile: (String) -> Unit,
    viewModel: OnboardTruckOwnerViewModel = hiltViewModel(),
) {
    val fullName by viewModel.fullName.collectAsState()
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val alternativePhone by viewModel.alternativePhone.collectAsState()
    val aadharCard by viewModel.aadharCard.collectAsState()
    val panCard by viewModel.panCard.collectAsState()
    val businessName by viewModel.businessName.collectAsState()
    val gstNumber by viewModel.gstNumber.collectAsState()
    val isTdsApplicable by viewModel.isTdsApplicable.collectAsState()
    val accountHolderName by viewModel.accountHolderName.collectAsState()
    val accountNumber by viewModel.accountNumber.collectAsState()
    val ifscCode by viewModel.ifscCode.collectAsState()
    val upiId by viewModel.upiId.collectAsState()
    val bankName by viewModel.bankName.collectAsState()
    val branchName by viewModel.branchName.collectAsState()
    val baseCity by viewModel.baseCity.collectAsState()
    val baseCityQuery by viewModel.baseCityQuery.collectAsState()
    val baseCitySuggestions by viewModel.baseCitySuggestions.collectAsState()
    val baseCityLoading by viewModel.baseCitySearchLoading.collectAsState()
    val fromCityQuery by viewModel.fromCityQuery.collectAsState()
    val toCityQuery by viewModel.toCityQuery.collectAsState()
    val fromCitySuggestions by viewModel.fromCitySuggestions.collectAsState()
    val toCitySuggestions by viewModel.toCitySuggestions.collectAsState()
    val fromCityLoading by viewModel.fromCityLoading.collectAsState()
    val toCityLoading by viewModel.toCityLoading.collectAsState()
    val visibleRoutes by viewModel.visibleRoutes.collectAsState()
    val showAddRouteSheet by viewModel.showAddRouteSheet.collectAsState()
    val routeSheetError by viewModel.routeSheetError.collectAsState()
    val selectedFromCity by viewModel.selectedFromCity.collectAsState()
    val selectedToCity by viewModel.selectedToCity.collectAsState()
    val selectedPriority by viewModel.selectedPriority.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val createdTruckOwner by viewModel.createdTruckOwner.collectAsState()

    val canSubmit = viewModel.canSubmit()
    val submitBlockReason = viewModel.submitBlockReason()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messageEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(
        fullName,
        phone,
        aadharCard,
        panCard,
        accountHolderName,
        accountNumber,
        ifscCode,
        canSubmit,
        submitBlockReason
    ) {
        if (!canSubmit) {
            Log.d(
                "OnboardTruckOwnerScreen",
                "Save disabled. Reason=$submitBlockReason [full=${fullName.isNotBlank()}, phoneLen=${phone.length}, aadhaarLen=${aadharCard.length}, panLen=${panCard.length}, accHolder=${accountHolderName.isNotBlank()}, accLen=${accountNumber.length}, ifscLen=${ifscCode.length}]"
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Onboard Truck Owner",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {
                ProfileSection(
                    title = "PERSONAL INFORMATION",
                    icon = Icons.Default.Person,
                    initiallyExpanded = true,
                ) {
                    ProfileField(
                        label = "FULL NAME",
                        value = fullName,
                        leadingIcon = Icons.Default.Person,
                        required = true,
                        onValueChange = viewModel::onFullNameChanged,
                    )
                    DividerLine()
                    ProfileField(
                        label = "EMAIL ADDRESS",
                        value = email,
                        leadingIcon = Icons.Default.Badge,
                        validationType = InputValidationType.EMAIL,
                        onValueChange = viewModel::onEmailChanged,
                    )
                    DividerLine()
                    ProfileField(
                        label = "PHONE NUMBER",
                        value = phone,
                        leadingIcon = Icons.Default.Phone,
                        validationType = InputValidationType.PHONE,
                        required = true,
                        onValueChange = viewModel::onPhoneChanged,
                    )
                    DividerLine()
                    ProfileField(
                        label = "ALTERNATIVE PHONE",
                        value = alternativePhone,
                        leadingIcon = Icons.Default.Phone,
                        validationType = InputValidationType.PHONE,
                        onValueChange = viewModel::onAlternativePhoneChanged,
                    )
                    DividerLine()
                    ProfileField(
                        label = "AADHAAR NUMBER",
                        value = aadharCard,
                        leadingIcon = Icons.Default.Fingerprint,
                        validationType = InputValidationType.AADHAAR,
                        required = true,
                        onValueChange = viewModel::onAadhaarChanged,
                    )
                    DividerLine()
                    ProfileField(
                        label = "PAN NUMBER",
                        value = panCard,
                        leadingIcon = Icons.Default.CreditCard,
                        validationType = InputValidationType.PAN,
                        required = true,
                        onValueChange = viewModel::onPanChanged,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                ProfileSection(
                    title = "BUSINESS DETAILS",
                    icon = Icons.Default.Business,
                    initiallyExpanded = true,
                ) {
                    ProfileField(
                        label = "BUSINESS NAME",
                        value = businessName,
                        leadingIcon = Icons.Default.Business,
                        onValueChange = viewModel::onBusinessNameChanged,
                    )
                    DividerLine()
                    ProfileField(
                        label = "GST NUMBER",
                        value = gstNumber,
                        leadingIcon = Icons.Default.Badge,
                        validationType = InputValidationType.GST,
                        onValueChange = viewModel::onGstChanged,
                    )
                    DividerLine()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "TDS APPLICABLE",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Switch(
                            checked = isTdsApplicable,
                            onCheckedChange = viewModel::onTdsApplicableChanged,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ProfileSection(
                    title = "BASE LOCATION",
                    icon = Icons.Default.LocationOn,
                    initiallyExpanded = true,
                ) {
                    CitySearchField(
                        query = baseCityQuery,
                        onQueryChange = viewModel::onBaseCityQueryChanged,
                        suggestions = baseCitySuggestions,
                        onCitySelected = viewModel::onBaseCitySelected,
                        isLoading = baseCityLoading,
                        label = "Base Location",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                ProfileSection(
                    title = "PREFERRED ROUTES",
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    initiallyExpanded = true,
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        visibleRoutes.forEach { route ->
                            RouteChip(
                                fromCity = route.sourceCityName,
                                toCity = route.destCityName,
                                isStaged = route.isStaged,
                                onDelete = { viewModel.onStageRouteDelete(route.id, route.isStaged) },
                            )
                        }
                    }
                    if (visibleRoutes.isEmpty()) {
                        Text(
                            "Add the routes you regularly cover",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        AssistChip(
                            onClick = viewModel::openAddRouteSheet,
                            label = { Text("+ Add route", style = MaterialTheme.typography.labelMedium) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                labelColor = MaterialTheme.colorScheme.primary,
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ProfileSection(
                    title = "BANKING INFORMATION",
                    icon = Icons.Default.AccountBalance,
                    initiallyExpanded = true,
                ) {
                    ProfileField(
                        label = "ACCOUNT HOLDER",
                        value = accountHolderName,
                        leadingIcon = Icons.Default.Person,
                        required = true,
                        onValueChange = viewModel::onAccountHolderNameChanged,
                    )
                    DividerLine()
                    ProfileField(
                        label = "ACCOUNT NUMBER",
                        value = accountNumber,
                        leadingIcon = Icons.Default.CreditCard,
                        validationType = InputValidationType.ACCOUNT_NUMBER,
                        required = true,
                        onValueChange = viewModel::onAccountNumberChanged,
                    )
                    DividerLine()
                    ProfileField(
                        label = "IFSC CODE",
                        value = ifscCode,
                        leadingIcon = Icons.Default.Badge,
                        validationType = InputValidationType.IFSC,
                        required = true,
                        onValueChange = viewModel::onIfscChanged,
                    )
                    DividerLine()
                    ProfileField(
                        label = "UPI ID",
                        value = upiId,
                        leadingIcon = Icons.Default.Badge,
                        validationType = InputValidationType.UPI,
                        onValueChange = viewModel::onUpiChanged,
                    )
                    DividerLine()
                    ProfileField(
                        label = "BANK NAME",
                        value = bankName,
                        leadingIcon = Icons.Default.AccountBalance,
                        onValueChange = viewModel::onBankNameChanged,
                    )
                    DividerLine()
                    ProfileField(
                        label = "BRANCH NAME",
                        value = branchName,
                        leadingIcon = Icons.AutoMirrored.Filled.Assignment,
                        onValueChange = viewModel::onBranchNameChanged,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                ProfileSection(
                    title = "DOCUMENTS",
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    initiallyExpanded = true,
                ) {
                    DocumentPlaceholderList()
                }

                Spacer(modifier = Modifier.height(140.dp))
            }

            FloatingActions(
                onCancel = onBackClick,
                onSave = { viewModel.submit() },
                enabled = canSubmit && !isSaving,
                isSaving = isSaving,
                blockReason = submitBlockReason,
                modifier = Modifier.align(Alignment.BottomCenter),
            )

            AnimatedVisibility(visible = isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Saving truck owner...")
                        }
                    }
                }
            }
        }
    }

    if (showAddRouteSheet) {
        AddRouteBottomSheet(
            fromQuery = fromCityQuery,
            onFromQueryChange = viewModel::onFromCityQueryChanged,
            fromSuggestions = fromCitySuggestions,
            onFromCitySelected = viewModel::onFromCitySelected,
            isFromLoading = fromCityLoading,
            selectedFromCity = selectedFromCity,
            isFromPreFilled = selectedFromCity?.id == baseCity?.id,
            toQuery = toCityQuery,
            onToQueryChange = viewModel::onToCityQueryChanged,
            toSuggestions = toCitySuggestions,
            onToCitySelected = viewModel::onToCitySelected,
            isToLoading = toCityLoading,
            selectedToCity = selectedToCity,
            selectedPriority = selectedPriority,
            onPrioritySelected = { viewModel.selectedPriority.value = it },
            onConfirm = viewModel::onStageRouteAdd,
            onDismiss = viewModel::dismissAddRouteSheet,
            errorMessage = routeSheetError,
        )
    }

    createdTruckOwner?.let { created ->
        TruckOwnerOnboardedDialog(
            truckOwnerName = created.fullName,
            canViewProfile = !created.userId.isNullOrBlank(),
            onDone = {
                viewModel.consumeCreatedTruckOwner()
                onCloseSuccess()
            },
            onViewProfile = {
                val userId = created.userId ?: return@TruckOwnerOnboardedDialog
                viewModel.consumeCreatedTruckOwner()
                onViewTruckOwnerProfile(userId)
            },
            onDismiss = viewModel::consumeCreatedTruckOwner,
        )
    }
}

@Composable
private fun ProfileSection(
    title: String,
    icon: ImageVector,
    initiallyExpanded: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    var isExpanded by rememberSaveable { mutableStateOf(initiallyExpanded) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandGreen,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = BrandGreen,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1f),
            )
//            VerificationChip("Unverified", Color(0xFFFFA000))
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { isExpanded = !isExpanded }) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = BrandGreen,
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    leadingIcon: ImageVector,
    required: Boolean = false,
    validationType: InputValidationType = InputValidationType.NONE,
    onValueChange: (String) -> Unit,
) {
    var hasLostFocusOnce by rememberSaveable { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val validationError = validationError(
        label = label,
        value = value,
        type = validationType,
        required = required,
        showIncompleteError = hasLostFocusOnce
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (required) "$label *" else label,
                style = MaterialTheme.typography.labelSmall,
                color = if (required) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state ->
                    if (isFocused && !state.isFocused) {
                        hasLostFocusOnce = true
                    }
                    isFocused = state.isFocused
                },
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            isError = validationError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = when (validationType) {
                    InputValidationType.PHONE,
                    InputValidationType.ACCOUNT_NUMBER,
                    InputValidationType.AADHAAR -> KeyboardType.Number
                    InputValidationType.EMAIL -> KeyboardType.Email
                    else -> KeyboardType.Text
                },
                capitalization = when (validationType) {
                    InputValidationType.PAN,
                    InputValidationType.IFSC,
                    InputValidationType.GST -> KeyboardCapitalization.Characters
                    InputValidationType.UPI,
                    InputValidationType.EMAIL -> KeyboardCapitalization.None
                    else -> KeyboardCapitalization.Words
                },
                imeAction = ImeAction.Next,
            ),
            visualTransformation = when (validationType) {
                InputValidationType.AADHAAR -> AadhaarVisualTransformation
                InputValidationType.PHONE -> PhoneVisualTransformation
                else -> VisualTransformation.None
            },
            supportingText = if (validationError != null) {
                {
                    Text(
                        text = validationError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            } else null,
            placeholder = when (validationType) {
                InputValidationType.AADHAAR -> { { Text("1234 5678 9012") } }
                InputValidationType.PAN -> { { Text("ABCDE1234F") } }
                InputValidationType.GST -> { { Text("27ABCDE1234F1Z5") } }
                InputValidationType.IFSC -> { { Text("HDFC0001234") } }
                InputValidationType.UPI -> { { Text("name@okhdfcbank") } }
                InputValidationType.EMAIL -> { { Text("email@example.com") } }
                else -> null
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (validationError == null) BrandGreen else MaterialTheme.colorScheme.error,
                unfocusedBorderColor = if (validationError == null) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.error,
                errorBorderColor = MaterialTheme.colorScheme.error,
            ),
        )
    }
}

@Composable
private fun DividerLine() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 1.dp,
    )
}

@Composable
private fun VerificationChip(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun DocumentPlaceholderList() {
    val docs = listOf(
        PlaceholderDocument(DocumentType.AADHAAR, DocumentStatus.UPLOADED),
        PlaceholderDocument(DocumentType.PAN, DocumentStatus.VERIFIED),
        PlaceholderDocument(DocumentType.GST_CERTIFICATE, DocumentStatus.UPLOADED),
        PlaceholderDocument(DocumentType.COMPANY_REGISTRATION, null),
        PlaceholderDocument(DocumentType.TDS_CERTIFICATE, DocumentStatus.UPLOADED),
        PlaceholderDocument(DocumentType.CANCELLED_CHEQUE, DocumentStatus.UPLOADED),
    )

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        docs.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = docIcon(item.type),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.type.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                )

//                item.status?.let {
//                    StatusBadge(it)
//                    Spacer(modifier = Modifier.width(8.dp))
//                }

                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = "Upload disabled",
                    tint = BrandGreen.copy(alpha = 0.35f),
                    modifier = Modifier.size(24.dp),
                )
            }

            if (index < docs.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: DocumentStatus) {
    val (text, color) = when (status) {
        DocumentStatus.VERIFIED -> "Verified" to BrandGreen
        DocumentStatus.UPLOADED -> "Verification Pending" to Color(0xFFFFA000)
        DocumentStatus.REJECTED -> "Rejected" to MaterialTheme.colorScheme.error
        DocumentStatus.PENDING -> "Pending" to MaterialTheme.colorScheme.error
        DocumentStatus.UNKNOWN -> "N/A" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun FloatingActions(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    enabled: Boolean,
    isSaving: Boolean,
    blockReason: String?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
//        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving,
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onSave,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (!enabled && !isSaving && !blockReason.isNullOrBlank()) {
            Text(
                text = blockReason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
            )
        }
    }
}

private data class PlaceholderDocument(
    val type: DocumentType,
    val status: DocumentStatus?,
)

private fun validationError(value: String, type: InputValidationType): String? {
    return validationError("", value, type, false, false)
}

private fun validationError(
    label: String,
    value: String,
    type: InputValidationType,
    required: Boolean,
    showIncompleteError: Boolean
): String? {
    if (required && value.isBlank() && showIncompleteError) {
        return "$label is required"
    }
    if (value.isBlank()) return null

    fun fromResult(result: ValidationResult): String? =
        if (result is ValidationResult.Error) result.message else null

    return when (type) {
        InputValidationType.AADHAAR -> {
            when {
                value.length < 12 && showIncompleteError -> "Aadhaar must be 12 digits"
                value.length == 12 -> fromResult(ProfileFieldValidators.validateAadhaar(value))
                else -> null
            }
        }
        InputValidationType.PAN -> {
            when {
                value.length < 10 && showIncompleteError -> "PAN must be 10 characters"
                value.length == 10 -> fromResult(ProfileFieldValidators.validatePan(value))
                else -> null
            }
        }
        InputValidationType.GST -> {
            when {
                value.length < 15 && showIncompleteError -> "GST must be 15 characters"
                value.length == 15 -> fromResult(ProfileFieldValidators.validateGst(value))
                else -> null
            }
        }
        InputValidationType.IFSC -> {
            when {
                value.length < 11 && showIncompleteError -> "IFSC must be 11 characters"
                value.length == 11 -> fromResult(ProfileFieldValidators.validateIfsc(value))
                else -> null
            }
        }
        InputValidationType.UPI -> if (value.contains("@")) fromResult(ProfileFieldValidators.validateUpiId(value)) else null
        InputValidationType.EMAIL -> fromResult(ProfileFieldValidators.validateEmail(value))
        InputValidationType.ACCOUNT_NUMBER -> {
            when {
                value.length < 9 && showIncompleteError -> "Account must be 9-18 digits"
                value.length >= 9 -> fromResult(ProfileFieldValidators.validateAccountNumber(value))
                else -> null
            }
        }
        InputValidationType.PHONE -> {
            when {
                value.length < 10 && showIncompleteError -> "Phone must be 10 digits"
                value.length == 10 -> fromResult(ProfileFieldValidators.validateIndianPhone(value))
                else -> null
            }
        }
        InputValidationType.NONE -> null
    }
}

private val AadhaarVisualTransformation = VisualTransformation { text ->
    val transformed = ProfileFieldValidators.formatAadhaar(text.text)
    TransformedText(
        AnnotatedString(transformed),
        object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 8) return offset + 1
                return offset + 2
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 9) return offset - 1
                return offset - 2
            }
        },
    )
}

private val PhoneVisualTransformation = VisualTransformation { text ->
    val transformed = ProfileFieldValidators.formatPhone(text.text)
    TransformedText(
        AnnotatedString(transformed),
        object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return if (offset <= 5) offset else offset + 1
            }

            override fun transformedToOriginal(offset: Int): Int {
                return if (offset <= 5) offset else offset - 1
            }
        },
    )
}

private fun docIcon(type: DocumentType): ImageVector = when (type) {
    DocumentType.PAN -> Icons.Default.CreditCard
    DocumentType.AADHAAR -> Icons.Default.Fingerprint
    DocumentType.GST_CERTIFICATE -> Icons.Default.Description
    DocumentType.COMPANY_REGISTRATION -> Icons.Default.Business
    DocumentType.TDS_CERTIFICATE -> Icons.Default.Badge
    DocumentType.CANCELLED_CHEQUE -> Icons.Default.AccountBalance
    else -> Icons.Default.Description
}

@Composable
private fun TruckOwnerOnboardedDialog(
    truckOwnerName: String,
    canViewProfile: Boolean,
    onDone: () -> Unit,
    onViewProfile: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val successColor = FreightThemeTokens.colors.successColor
    val onSuccessColor = FreightThemeTokens.colors.onSuccessColor
    val scrimColor = FreightThemeTokens.colors.dialogScrimColor
    val density = LocalDensity.current

    var playEntry by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { playEntry = true }

    val cardScale by animateFloatAsState(
        targetValue = if (playEntry) 1f else 0.97f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "dialogCardScale",
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (playEntry) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "dialogCardAlpha",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (playEntry) 1f else 0.85f,
        animationSpec = tween(durationMillis = 500, delayMillis = 150, easing = FastOutSlowInEasing),
        label = "successIconScale",
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (playEntry) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = 150, easing = FastOutSlowInEasing),
        label = "successIconAlpha",
    )
    val iconOvershoot by animateFloatAsState(
        targetValue = if (playEntry) 1.06f else 0.85f,
        animationSpec = tween(durationMillis = 360, delayMillis = 150, easing = FastOutSlowInEasing),
        label = "successIconOvershoot",
    )
    val checkProgress by animateFloatAsState(
        targetValue = if (playEntry) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 500, easing = FastOutSlowInEasing),
        label = "checkProgress",
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                modifier = Modifier
                    .width(320.dp)
                    .graphicsLayer {
                        alpha = cardAlpha
                        scaleX = cardScale
                        scaleY = cardScale
                    }
                    .border(
                        width = 1.dp,
                        color = colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(24.dp),
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(start = 28.dp, top = 36.dp, end = 28.dp, bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Canvas(modifier = Modifier.width(180.dp).height(1.dp)) {
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    successColor.copy(alpha = 0f),
                                    successColor.copy(alpha = 0.8f),
                                    successColor.copy(alpha = 0f),
                                )
                            ),
                            start = androidx.compose.ui.geometry.Offset(0f, size.height / 2f),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2f),
                            strokeWidth = size.height,
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        modifier = Modifier
                            .size(72.dp)
                            .graphicsLayer {
                                alpha = iconAlpha
                                scaleX = iconScale * iconOvershoot
                                scaleY = iconScale * iconOvershoot
                            }
                            .border(
                                width = 1.dp,
                                color = successColor.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(999.dp),
                            ),
                        shape = RoundedCornerShape(999.dp),
                        color = successColor.copy(alpha = 0.10f),
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width * 0.18f, size.height * 0.54f)
                                lineTo(size.width * 0.42f, size.height * 0.76f)
                                lineTo(size.width * 0.84f, size.height * 0.24f)
                            }
                            drawPath(
                                path = path,
                                color = successColor,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 2.2.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round,
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(path.getBounds().maxDimension * checkProgress, path.getBounds().maxDimension),
                                        0f,
                                    ),
                                ),
                            )
                        }
                    }

                    AnimatedDialogContent(delayMillis = 350, density = density) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "ONBOARDING COMPLETE",
                            fontSize = 11.sp,
                            letterSpacing = 0.12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = successColor,
                        )
                    }

                    AnimatedDialogContent(delayMillis = 400, density = density) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${truckOwnerName.ifBlank { "Truck owner" }} has been onboarded successfully.",
                            fontSize = 22.sp,
                            lineHeight = 28.6.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = (-0.02).sp,
                            textAlign = TextAlign.Center,
                            color = colorScheme.onSurface.copy(alpha = 0.92f),
                        )
                    }

                    AnimatedDialogContent(delayMillis = 500, density = density) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = successColor.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.border(
                                width = 0.5.dp,
                                color = successColor.copy(alpha = 0.20f),
                                shape = RoundedCornerShape(6.dp),
                            ),
                        ) {
                            Text(
                                text = "\u25cf TRUCK OWNER",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                fontSize = 11.sp,
                                letterSpacing = 0.06.sp,
                                fontWeight = FontWeight.Medium,
                                color = successColor.copy(alpha = 0.85f),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                    HorizontalDivider(
                        color = colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.5.dp,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    AnimatedDialogContent(delayMillis = 550, density = density) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            DialogActionButton(
                                text = "Done",
                                onClick = onDone,
                                modifier = Modifier.weight(1f),
                                containerColor = Color.Transparent,
                                contentColor = colorScheme.onSurface.copy(alpha = 0.65f),
                                borderColor = colorScheme.outline.copy(alpha = 0.5f),
                                filled = false,
                            )
                            DialogActionButton(
                                text = "View Profile \u2192",
                                onClick = onViewProfile,
                                modifier = Modifier.weight(1.4f),
                                containerColor = successColor,
                                contentColor = onSuccessColor,
                                borderColor = successColor,
                                filled = true,
                                enabled = canViewProfile,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedDialogContent(
    delayMillis: Int,
    density: androidx.compose.ui.unit.Density,
    content: @Composable ColumnScope.() -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = delayMillis, easing = FastOutSlowInEasing),
        label = "dialogContentAlpha",
    )
    val translateY by animateFloatAsState(
        targetValue = if (visible) 0f else with(density) { 8.dp.toPx() },
        animationSpec = tween(durationMillis = 400, delayMillis = delayMillis, easing = FastOutSlowInEasing),
        label = "dialogContentTranslateY",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = translateY
        },
        content = content,
    )
}

@Composable
private fun DialogActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    filled: Boolean,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "dialogButtonScale",
    )

    if (filled) {
        Button(
            onClick = onClick,
            modifier = modifier.scale(scale).height(52.dp),
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
        ) {
            Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.scale(scale).height(52.dp),
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            interactionSource = interactionSource,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
        ) {
            Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
