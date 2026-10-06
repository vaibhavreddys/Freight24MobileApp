package com.freight24.connect.feature.clients.views
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.connect.feature.clients.domain.viewmodel.OnboardClientViewModel
import com.freight24.ui.components.DashedUploadCard
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.theme.FreightThemeTokens
import kotlinx.coroutines.launch
import java.io.File

private enum class ClientFieldType {
    TEXT,
    PHONE,
    EMAIL,
    PAN,
    GST,
    ADDRESS,
}

private enum class RequiredClientField {
    FULL_NAME,
    PHONE_NUMBER,
    PAN_NUMBER,
    GST_NUMBER,
    BRANCH_ENTRY,
    BRANCH_NAME,
    BRANCH_ADDRESS,
}

private data class MissingRequiredField(
    val field: RequiredClientField,
    val branchIndex: Int? = null,
    val branchId: Int? = null,
)

private enum class ContactSection(val title: String) {
    PAYMENT("PAYMENT"),
    TRAFFIC("TRAFFIC"),
    POD("POD"),
    TRACKING("TRACKING"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardClientScreen(
    onBackClick: () -> Unit,
    onCloseSuccess: () -> Unit,
    onViewClientProfile: (String) -> Unit,
    viewModel: OnboardClientViewModel = hiltViewModel(),
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val fullName by viewModel.fullName.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val panNumber by viewModel.panNumber.collectAsState()
    val email by viewModel.email.collectAsState()
    val companyName by viewModel.companyName.collectAsState()
    val gstNumber by viewModel.gstNumber.collectAsState()
    val branches by viewModel.branches.collectAsState()
    val clientType by viewModel.clientType.collectAsState()
    val companyType by viewModel.companyType.collectAsState()
    val operatingStructure by viewModel.operatingStructure.collectAsState()
    val states by viewModel.states.collectAsState()
    val citiesByState by viewModel.citiesByState.collectAsState()
    val branchTypes by viewModel.branchTypes.collectAsState()
    val contactRoles by viewModel.contactRoles.collectAsState()
    val completedStep by viewModel.completedStep.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val createdClient by viewModel.createdClient.collectAsState()
    val panDuplicateError by viewModel.panDuplicateError.collectAsState()
    val gstDuplicateError by viewModel.gstDuplicateError.collectAsState()

    val panFile by viewModel.panFile.collectAsState()
    val gstFile by viewModel.gstFile.collectAsState()
    val companyRegistrationFile by viewModel.companyRegistrationFile.collectAsState()
    val contractFile by viewModel.contractFile.collectAsState()

    var clientExpanded by rememberSaveable { mutableStateOf(true) }
    var documentsExpanded by rememberSaveable { mutableStateOf(true) }
    var saveAttempted by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    var showSourcePicker by remember { mutableStateOf(false) }
    var currentTargetPrefix by remember { mutableStateOf<String?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val sheetState = rememberModalBottomSheetState()
    val branchDetailsExpanded = remember { mutableStateMapOf<Int, Boolean>() }
    val teamContactsExpanded = remember { mutableStateMapOf<Int, Boolean>() }

    val snackbarHostState = remember { SnackbarHostState() }
    val successColor = FreightThemeTokens.colors.success
    val warningColor = FreightThemeTokens.colors.warning
    val infoColor = FreightThemeTokens.colors.info

    var pendingUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isPdfPending by remember { mutableStateOf(false) }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(),
        ) { uris ->
            if (uris.isNotEmpty()) {
                pendingUris = uris
                isPdfPending = false
            } else {
                currentTargetPrefix = null
            }
        }

    val docLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) {
                pendingUris = listOf(uri)
                isPdfPending = true
            } else {
                currentTargetPrefix = null
            }
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture(),
        ) { success ->
            if (success && tempCameraUri != null) {
                pendingUris = listOf(tempCameraUri!!)
                isPdfPending = false
            } else {
                currentTargetPrefix = null
            }
            tempCameraUri = null
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                val uri = createTempImageUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            }
        }

    fun initiateCapture() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val uri = createTempImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val hasRequiredValues = contactRoles.isNotEmpty() && viewModel.canSubmit()

    LaunchedEffect(Unit) {
        viewModel.messageEvent.collect { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(branches) {
        branches.forEach { branch ->
            if (branchDetailsExpanded[branch.id] == null) branchDetailsExpanded[branch.id] = true
            if (teamContactsExpanded[branch.id] == null) teamContactsExpanded[branch.id] = true
        }
    }

    fun firstMissingRequiredField(): MissingRequiredField? =
        when {
            fullName.trim().isBlank() -> MissingRequiredField(RequiredClientField.FULL_NAME)
            phoneNumber.trim().isBlank() -> MissingRequiredField(RequiredClientField.PHONE_NUMBER)
            panNumber.trim().isBlank() -> MissingRequiredField(RequiredClientField.PAN_NUMBER)
            companyName.trim().isBlank() -> MissingRequiredField(RequiredClientField.FULL_NAME)
            branches.isEmpty() -> MissingRequiredField(RequiredClientField.BRANCH_ENTRY)
            else -> {
                val branchIndex = branches.indexOfFirst { it.branchName.trim().isBlank() || it.branchAddress.trim().isBlank() }
                if (branchIndex >= 0) {
                    val branch = branches[branchIndex]
                    val field = if (branch.branchName.trim().isBlank()) RequiredClientField.BRANCH_NAME else RequiredClientField.BRANCH_ADDRESS
                    MissingRequiredField(field = field, branchIndex = branchIndex, branchId = branch.id)
                } else {
                    null
                }
            }
        }

    fun scrollToBranch(branchIndex: Int) {
        // 0: client info, 1..N: branch cards
        scope.launch { listState.animateScrollToItem(1 + branchIndex) }
    }

    fun onSaveClick() {
        saveAttempted = true
        when (val firstMissing = firstMissingRequiredField()) {
            null -> viewModel.submit()
            else -> {
                when (firstMissing.field) {
                    RequiredClientField.FULL_NAME,
                    RequiredClientField.PHONE_NUMBER,
                    RequiredClientField.PAN_NUMBER,
                    RequiredClientField.GST_NUMBER,
                    -> {
                        clientExpanded = true
                        scope.launch { listState.animateScrollToItem(0) }
                    }

                    RequiredClientField.BRANCH_ENTRY -> {
                        scope.launch { listState.animateScrollToItem(1) }
                    }

                    RequiredClientField.BRANCH_NAME,
                    RequiredClientField.BRANCH_ADDRESS,
                    -> {
                        firstMissing.branchId?.let { branchDetailsExpanded[it] = true }
                        firstMissing.branchIndex?.let(::scrollToBranch)
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FreightTopAppBar(
                title = {
                    Text(
                        text = "Onboard Client",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    OnboardSection(
                        title = "Client Information",
                        accentColor = successColor,
                        icon = Icons.Default.Person,
                        isExpanded = clientExpanded,
                        onToggle = { clientExpanded = !clientExpanded },
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        SectionCard {
                            SelectionField(
                                label = "Client Type",
                                value = if (clientType == "DIRECT_CUSTOMER") "Direct Customer" else "Transporter",
                                options = listOf("DIRECT_CUSTOMER" to "Direct Customer", "TRANSPORTER" to "Transporter"),
                                onSelect = viewModel::onClientTypeChanged,
                            )
                            InternalDivider()
                            SelectionField(
                                label = "Company Type",
                                value = companyType,
                                options = listOf("Private Limited", "Public Limited", "LLP", "Partnership", "Sole Proprietor", "Trust", "HUF", "Other").map { it to it },
                                onSelect = viewModel::onCompanyTypeChanged,
                            )
                            InternalDivider()
                            FormField(
                                label = "Display / Primary Contact Name",
                                value = fullName,
                                icon = Icons.Default.Person,
                                required = true,
                                fieldType = ClientFieldType.TEXT,
                                errorMessage = if (saveAttempted && fullName.isBlank()) "Full Name is required" else null,
                                onValueChange = viewModel::onFullNameChanged,
                            )
                            InternalDivider()
                            FormField(
                                label = "Phone Number",
                                value = phoneNumber,
                                icon = Icons.Default.Call,
                                required = true,
                                fieldType = ClientFieldType.PHONE,
                                errorMessage = if (saveAttempted && phoneNumber.isBlank()) "Phone Number is required" else null,
                                onValueChange = viewModel::onPhoneNumberChanged,
                            )
                            InternalDivider()
                            FormField(
                                label = "PAN Number",
                                value = panNumber,
                                icon = Icons.Default.Badge,
                                required = true,
                                fieldType = ClientFieldType.PAN,
                                errorMessage = if (saveAttempted && panNumber.isBlank()) "PAN Number is required" else panDuplicateError,
                                onValueChange = viewModel::onPanNumberChanged,
                                onFocusLost = viewModel::onPanFocusLost,
                            )
                            InternalDivider()
                            FormField(
                                label = "Email Address",
                                value = email,
                                icon = Icons.Default.Email,
                                fieldType = ClientFieldType.EMAIL,
                                onValueChange = viewModel::onEmailChanged,
                            )
                            InternalDivider()
                            FormField(
                                label = "Legal Company Name",
                                value = companyName,
                                icon = Icons.Default.Apartment,
                                required = true,
                                fieldType = ClientFieldType.TEXT,
                                onValueChange = viewModel::onCompanyNameChanged,
                            )
                            InternalDivider()
                            FormField(
                                label = "GST Number",
                                value = gstNumber,
                                icon = Icons.Default.Badge,
                                fieldType = ClientFieldType.GST,
                                errorMessage = gstDuplicateError,
                                onValueChange = viewModel::onGstNumberChanged,
                                onFocusLost = viewModel::onGstFocusLost,
                            )
                        }
                    }
                }

                item {
                    SectionCard {
                        SelectionField(
                            label = "Operating Structure",
                            value = operatingStructure.lowercase().replaceFirstChar(Char::uppercase),
                            options = listOf("SINGLE" to "Single", "MULTIPLE" to "Multiple", "SKIP" to "Skip branches"),
                            onSelect = viewModel::onOperatingStructureChanged,
                        )
                    }
                }

                (if (operatingStructure == "MULTIPLE") branches else branches.take(1)).forEachIndexed { index, branch ->
                    item(key = "branch-${branch.id}") {
                        SectionCard {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = if (index == 0) "Registered / Head Office" else "Branch ${index + 1}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            InternalDivider()
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                OnboardSection(
                                    title = "Branch Details",
                                    accentColor = infoColor,
                                    icon = Icons.Default.LocationOn,
                                    isExpanded = branchDetailsExpanded[branch.id] ?: true,
                                    onToggle = {
                                        val current = branchDetailsExpanded[branch.id] ?: true
                                        branchDetailsExpanded[branch.id] = !current
                                    },
                                ) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    SectionCard {
                                        FormField(
                                            label = "Branch Name",
                                            value = branch.branchName,
                                            icon = Icons.Default.Badge,
                                            required = true,
                                            fieldType = ClientFieldType.TEXT,
                                            errorMessage = if (saveAttempted && branch.branchName.isBlank()) "Branch Name is required" else null,
                                            onValueChange = { viewModel.onBranchNameChanged(branch.id, it) },
                                        )
                                        InternalDivider()
                                        FormField(
                                            label = "Branch Code",
                                            value = branch.branchCode,
                                            icon = Icons.Default.Badge,
                                            fieldType = ClientFieldType.TEXT,
                                            onValueChange = { viewModel.onBranchCodeChanged(branch.id, it) },
                                        )
                                        if (operatingStructure == "MULTIPLE") {
                                            InternalDivider()
                                            SelectionField(
                                                label = "Branch Type",
                                                value = branchTypes.firstOrNull { it.id == branch.branchTypeId }?.name.orEmpty(),
                                                options = branchTypes.map { it.id to it.name },
                                                onSelect = { viewModel.onBranchTypeChanged(branch.id, it) },
                                            )
                                        }
                                        InternalDivider()
                                        FormField(
                                            label = "Address Line 1",
                                            value = branch.branchAddress,
                                            icon = Icons.Default.LocationOn,
                                            required = true,
                                            fieldType = ClientFieldType.ADDRESS,
                                            errorMessage = if (saveAttempted && branch.branchAddress.isBlank()) "Branch Address is required" else null,
                                            onValueChange = { viewModel.onBranchAddressChanged(branch.id, it) },
                                        )
                                        InternalDivider()
                                        SelectionField(
                                            label = "State",
                                            value = states.firstOrNull { it.id == branch.stateId }?.name.orEmpty(),
                                            options = states.map { it.id.toString() to it.name },
                                            onSelect = { viewModel.onBranchStateChanged(branch.id, it.toInt()) },
                                        )
                                        InternalDivider()
                                        SelectionField(
                                            label = "City",
                                            value = citiesByState[branch.stateId]?.firstOrNull { it.id == branch.cityId }?.name.orEmpty(),
                                            options = citiesByState[branch.stateId].orEmpty().map { it.id.toString() to it.name },
                                            onSelect = { viewModel.onBranchCityChanged(branch.id, it.toInt()) },
                                        )
                                        InternalDivider()
                                        FormField(
                                            label = "PIN Code",
                                            value = branch.pinCode,
                                            icon = Icons.Default.LocationOn,
                                            required = true,
                                            fieldType = ClientFieldType.PHONE,
                                            onValueChange = { viewModel.onBranchPinCodeChanged(branch.id, it) },
                                        )
                                    }
                                }

                                OnboardSection(
                                    title = "Team Contacts",
                                    accentColor = warningColor,
                                    icon = Icons.Default.Call,
                                    isExpanded = teamContactsExpanded[branch.id] ?: true,
                                    onToggle = {
                                        val current = teamContactsExpanded[branch.id] ?: true
                                        teamContactsExpanded[branch.id] = !current
                                    },
                                ) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    SectionCard {
                                        TeamContactBlock(
                                            contactSection = ContactSection.PAYMENT,
                                            name = branch.paymentContactName,
                                            phone = branch.paymentContactNumber,
                                            onCopyFromPrimary = { viewModel.copyPaymentFromPrimary(branch.id) },
                                            onNameChange = { viewModel.onPaymentContactNameChanged(branch.id, it) },
                                            onPhoneChange = { viewModel.onPaymentContactNumberChanged(branch.id, it) },
                                        )
                                        InternalDivider()
                                        TeamContactBlock(
                                            contactSection = ContactSection.TRAFFIC,
                                            name = branch.trafficContactName,
                                            phone = branch.trafficContactNumber,
                                            onCopyFromPrimary = { viewModel.copyTrafficFromPrimary(branch.id) },
                                            onNameChange = { viewModel.onTrafficContactNameChanged(branch.id, it) },
                                            onPhoneChange = { viewModel.onTrafficContactNumberChanged(branch.id, it) },
                                        )
                                        InternalDivider()
                                        TeamContactBlock(
                                            contactSection = ContactSection.POD,
                                            name = branch.podContactName,
                                            phone = branch.podContactNumber,
                                            onCopyFromPrimary = { viewModel.copyPodFromPrimary(branch.id) },
                                            onNameChange = { viewModel.onPodContactNameChanged(branch.id, it) },
                                            onPhoneChange = { viewModel.onPodContactNumberChanged(branch.id, it) },
                                        )
                                        InternalDivider()
                                        TeamContactBlock(
                                            contactSection = ContactSection.TRACKING,
                                            name = branch.trackingContactName,
                                            phone = branch.trackingContactNumber,
                                            onCopyFromPrimary = { viewModel.copyTrackingFromPrimary(branch.id) },
                                            onNameChange = { viewModel.onTrackingContactNameChanged(branch.id, it) },
                                            onPhoneChange = { viewModel.onTrackingContactNumberChanged(branch.id, it) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OnboardSection(
                        title = "Documents",
                        accentColor = infoColor,
                        icon = Icons.Default.Badge,
                        isExpanded = documentsExpanded,
                        onToggle = { documentsExpanded = !documentsExpanded },
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        SectionCard {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                DashedUploadCard(
                                    title = "PAN Card",
                                    subtitle = "Camera, JPG or PDF",
                                    label = "Optional",
                                    isUploaded = panFile != null,
                                    onUploadClick = {
                                        currentTargetPrefix = "PAN"
                                        showSourcePicker = true
                                    },
                                )
                                DashedUploadCard(
                                    title = "GST Certificate",
                                    subtitle = "Camera, JPG or PDF",
                                    label = "Optional",
                                    isUploaded = gstFile != null,
                                    onUploadClick = { currentTargetPrefix = "GST"; showSourcePicker = true },
                                )
                                DashedUploadCard(
                                    title = "Company Registration",
                                    subtitle = "Camera, JPG or PDF",
                                    label = "Optional",
                                    isUploaded = companyRegistrationFile != null,
                                    onUploadClick = { currentTargetPrefix = "COMPANY"; showSourcePicker = true },
                                )
                                DashedUploadCard(
                                    title = "Client Contract",
                                    subtitle = "Camera, JPG or PDF",
                                    label = "Optional",
                                    isUploaded = contractFile != null,
                                    onUploadClick = { currentTargetPrefix = "CONTRACT"; showSourcePicker = true },
                                )
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (saveAttempted && branches.isEmpty()) {
                            Text(
                                text = "At least one branch is required",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (operatingStructure == "MULTIPLE") OutlinedButton(
                            onClick = {
                                viewModel.addBranch()
                                saveAttempted = false
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(0.5.dp, successColor.copy(alpha = 0.5f)),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = successColor,
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add Branch",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            FloatingActions(
                onCancel = onBackClick,
                onSave = ::onSaveClick,
                enabled = hasRequiredValues,
                isSaving = isSaving,
                modifier = Modifier.align(Alignment.BottomCenter),
            )

            if (isSaving) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(FreightThemeTokens.colors.dialogScrim),
                    contentAlignment = Alignment.Center,
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 3.dp,
                                color = successColor,
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                when (completedStep) {
                                    0 -> "Creating client profile..."
                                    1 -> "Saving client locations..."
                                    else -> "Saving client contacts..."
                                },
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            com.freight24.ui.components.FileSourcePickerSheet(
                showSheet = showSourcePicker,
                onDismissRequest = { showSourcePicker = false },
                onCameraClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSourcePicker = false
                        initiateCapture()
                    }
                },
                onGalleryClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSourcePicker = false
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                },
                onDocumentClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSourcePicker = false
                        docLauncher.launch(arrayOf("application/pdf"))
                    }
                },
                sheetState = sheetState,
            )

            if (pendingUris.isNotEmpty()) {
                com.freight24.ui.components.FileConfirmationSheet(
                    selectedUris = pendingUris,
                    isPdf = isPdfPending,
                    isUploading = isSaving,
                    uploadButtonText = "Confirm",
                    onUploadClick = {
                        currentTargetPrefix?.let { prefix ->
                            viewModel.onFileSelected(context, pendingUris, prefix, isPdfPending) {
                                pendingUris = emptyList()
                                currentTargetPrefix = null
                            }
                        }
                    },
                    onDismiss = {
                        pendingUris = emptyList()
                        currentTargetPrefix = null
                    },
                )
            }
        }
    }

    createdClient?.let { created ->
        EntityOnboardedDialog(
            entityName = created.fullName,
            entityLabel = "CLIENT",
            canViewProfile = !created.userId.isNullOrBlank(),
            onDone = {
                viewModel.consumeCreatedClient()
                onCloseSuccess()
            },
            onViewProfile = {
                val userId = created.userId ?: return@EntityOnboardedDialog
                viewModel.consumeCreatedClient()
                onViewClientProfile(userId)
            },
            onDismiss = viewModel::consumeCreatedClient,
        )
    }
}

@Composable
private fun OnboardSection(
    title: String,
    accentColor: Color,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "sectionRotation",
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.12f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = accentColor, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = accentColor,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.rotate(rotation),
            )
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(content = content)
    }
}

@Composable
private fun InternalDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
        thickness = 0.5.dp,
    )
}

@Composable
private fun SelectionField(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = "$label *",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = options.isNotEmpty(),
            ) {
                Text(value.ifBlank { "Select $label" }, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { (key, text) ->
                    DropdownMenuItem(
                        text = { Text(text) },
                        onClick = {
                            onSelect(key)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamContactBlock(
    contactSection: ContactSection,
    name: String,
    phone: String,
    onCopyFromPrimary: () -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
) {
    Column(modifier = Modifier.animateContentSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 14.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(8.dp).background(contactSectionColor(contactSection), CircleShape))
            Spacer(modifier = Modifier.width(9.dp))
            Text(
                text = contactSection.title,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.09.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Surface(
                modifier = Modifier.clickable(onClick = onCopyFromPrimary),
                shape = RoundedCornerShape(8.dp),
                color = FreightThemeTokens.colors.success.copy(alpha = 0.08f),
                border = BorderStroke(0.5.dp, FreightThemeTokens.colors.success.copy(alpha = 0.20f)),
            ) {
                Text(
                    text = "Copy from primary",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Medium,
                    color = FreightThemeTokens.colors.success,
                )
            }
        }
        FormField(
            label = "Full Name",
            value = name,
            icon = Icons.Default.Person,
            fieldType = ClientFieldType.TEXT,
            onValueChange = onNameChange,
        )
        FormField(
            label = "Phone Number",
            value = phone,
            icon = Icons.Default.Call,
            fieldType = ClientFieldType.PHONE,
            onValueChange = onPhoneChange,
        )
    }
}

@Composable
private fun contactSectionColor(contactSection: ContactSection): Color {
    val colors = FreightThemeTokens.colors
    return when (contactSection) {
        ContactSection.PAYMENT -> colors.success
        ContactSection.TRAFFIC -> colors.info
        ContactSection.POD -> colors.warning
        ContactSection.TRACKING -> MaterialTheme.colorScheme.tertiary
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    icon: ImageVector,
    required: Boolean = false,
    fieldType: ClientFieldType,
    errorMessage: String? = null,
    onValueChange: (String) -> Unit,
    onFocusLost: (() -> Unit)? = null,
) {
    val successColor = FreightThemeTokens.colors.success
    val monospace = fieldType == ClientFieldType.GST || fieldType == ClientFieldType.PAN
    val isMultiline = fieldType == ClientFieldType.ADDRESS

    Column(modifier = Modifier.fillMaxWidth()) {
        TextLabelRow(label = label, icon = icon, required = required)
        var isFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = if (errorMessage != null) 0.dp else 13.dp)
                    .then(if (isMultiline) Modifier.heightIn(min = 80.dp) else Modifier)
                    .onFocusChanged { state ->
                        if (isFocused && !state.isFocused) {
                            onFocusLost?.invoke()
                        }
                        isFocused = state.isFocused
                    },
            singleLine = !isMultiline,
            minLines = if (isMultiline) 3 else 1,
            maxLines = if (isMultiline) Int.MAX_VALUE else 1,
            shape = MaterialTheme.shapes.small,
            isError = errorMessage != null,
            textStyle =
                MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
                    letterSpacing = if (monospace) 0.05.sp else 0.sp,
                ),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        when (fieldType) {
                            ClientFieldType.PHONE -> KeyboardType.Phone
                            ClientFieldType.EMAIL -> KeyboardType.Email
                            ClientFieldType.GST,
                            ClientFieldType.PAN,
                            -> KeyboardType.Ascii
                            else -> KeyboardType.Text
                        },
                    capitalization =
                        when (fieldType) {
                            ClientFieldType.EMAIL -> KeyboardCapitalization.None
                            ClientFieldType.GST,
                            ClientFieldType.PAN,
                            -> KeyboardCapitalization.Characters
                            else -> KeyboardCapitalization.Words
                        },
                    imeAction = if (isMultiline) ImeAction.Default else ImeAction.Next,
                ),
            visualTransformation = VisualTransformation.None,
            placeholder = {
                Text(
                    text =
                        when (fieldType) {
                            ClientFieldType.PHONE -> "Enter phone number"
                            ClientFieldType.EMAIL -> "email@example.com"
                            ClientFieldType.PAN -> "ABCDE1234F"
                            ClientFieldType.GST -> "27ABCDE1234F1Z5"
                            ClientFieldType.ADDRESS -> "Enter branch address"
                            else -> label
                        },
                    fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
                    style = MaterialTheme.typography.bodyMedium,
                    letterSpacing = if (monospace) 0.05.sp else 0.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                )
            },
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = if (errorMessage == null) successColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error,
                    unfocusedBorderColor = if (errorMessage == null) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.error,
                    cursorColor = successColor,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun TextLabelRow(
    label: String,
    icon: ImageVector,
    required: Boolean,
) {
    val labelColor =
        if (required) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
        }

    Row(
        modifier = Modifier.padding(start = 16.dp, top = 13.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text =
                buildAnnotatedString {
                    append(label.uppercase())
                    if (required) {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) {
                            append(" *")
                        }
                    }
                },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.07.sp,
            color = labelColor,
        )
    }
}

@Composable
private fun FloatingActions(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    enabled: Boolean,
    isSaving: Boolean,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "saveButtonScale",
    )

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(54.dp),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            ) {
                Text("Cancel", fontWeight = FontWeight.Medium)
            }
            Button(
                onClick = onSave,
                enabled = enabled,
                interactionSource = interactionSource,
                modifier = Modifier.weight(1.9f).height(54.dp).scale(scale),
                shape = MaterialTheme.shapes.medium,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = FreightThemeTokens.colors.success,
                        contentColor = FreightThemeTokens.colors.onSuccess,
                        disabledContainerColor = FreightThemeTokens.colors.success.copy(alpha = 0.4f),
                        disabledContentColor = FreightThemeTokens.colors.onSuccess.copy(alpha = 0.6f),
                    ),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = FreightThemeTokens.colors.onSuccess,
                    )
                } else {
                    Text("Save Changes \u2192", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun EntityOnboardedDialog(
    entityName: String,
    entityLabel: String,
    canViewProfile: Boolean,
    onDone: () -> Unit,
    onViewProfile: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val successColor = FreightThemeTokens.colors.success
    val onSuccessColor = FreightThemeTokens.colors.onSuccess
    val scrimColor = FreightThemeTokens.colors.dialogScrim
    var playEntry by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { playEntry = true }

    val cardScale by animateFloatAsState(if (playEntry) 1f else 0.97f, tween(400, easing = FastOutSlowInEasing), label = "cardScale")
    val cardAlpha by animateFloatAsState(if (playEntry) 1f else 0f, tween(400, easing = FastOutSlowInEasing), label = "cardAlpha")
    val iconScale by animateFloatAsState(if (playEntry) 1f else 0.85f, tween(500, 150, easing = FastOutSlowInEasing), label = "iconScale")
    val iconAlpha by animateFloatAsState(if (playEntry) 1f else 0f, tween(500, 150, easing = FastOutSlowInEasing), label = "iconAlpha")
    val checkProgress by animateFloatAsState(
        if (playEntry) 1f else 0f,
        tween(400, 500, easing = FastOutSlowInEasing),
        label = "checkProgress",
    )

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier.fillMaxSize().background(scrimColor),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                modifier =
                    Modifier
                        .width(320.dp)
                        .graphicsLayer {
                            alpha = cardAlpha
                            scaleX = cardScale
                            scaleY = cardScale
                        }
                        .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(start = 28.dp, top = 36.dp, end = 28.dp, bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Canvas(modifier = Modifier.width(180.dp).height(1.dp)) {
                        drawLine(
                            brush =
                                Brush.horizontalGradient(
                                    listOf(
                                        successColor.copy(alpha = 0f),
                                        successColor.copy(alpha = 0.8f),
                                        successColor.copy(alpha = 0f),
                                    ),
                                ),
                            start = androidx.compose.ui.geometry.Offset(0f, size.height / 2f),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2f),
                            strokeWidth = size.height,
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Surface(
                        modifier =
                            Modifier
                                .size(72.dp)
                                .graphicsLayer {
                                    alpha = iconAlpha
                                    scaleX = iconScale
                                    scaleY = iconScale
                                }
                                .border(1.dp, successColor.copy(alpha = 0.25f), RoundedCornerShape(999.dp)),
                        shape = RoundedCornerShape(999.dp),
                        color = successColor.copy(alpha = 0.10f),
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                            val path =
                                androidx.compose.ui.graphics.Path().apply {
                                    moveTo(size.width * 0.18f, size.height * 0.54f)
                                    lineTo(size.width * 0.42f, size.height * 0.76f)
                                    lineTo(size.width * 0.84f, size.height * 0.24f)
                                }
                            drawPath(
                                path = path,
                                color = successColor,
                                style =
                                    Stroke(
                                        width = 2.2.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round,
                                        pathEffect =
                                            PathEffect.dashPathEffect(
                                                floatArrayOf(path.getBounds().maxDimension * checkProgress, path.getBounds().maxDimension),
                                                0f,
                                            ),
                                    ),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("ONBOARDING COMPLETE", letterSpacing = 0.12.sp, fontWeight = FontWeight.SemiBold, color = successColor)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${entityName.ifBlank {
                            entityLabel.lowercase().replaceFirstChar(
                                Char::titlecase,
                            )
                        }} has been onboarded successfully.",
                        style = MaterialTheme.typography.titleLarge,
                        lineHeight = 28.6.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.02).sp,
                        textAlign = TextAlign.Center,
                        color = colorScheme.onSurface.copy(alpha = 0.92f),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = successColor.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.border(0.5.dp, successColor.copy(alpha = 0.20f), RoundedCornerShape(6.dp)),
                    ) {
                        Text(
                            text = "\u25cf $entityLabel",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 0.06.sp,
                            fontWeight = FontWeight.Medium,
                            color = successColor.copy(alpha = 0.85f),
                        )
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    HorizontalDivider(
                        color = colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.5.dp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DialogButton(
                            text = "Done",
                            onClick = onDone,
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                            contentColor = colorScheme.onSurface.copy(alpha = 0.65f),
                            borderColor = colorScheme.outline.copy(alpha = 0.5f),
                            filled = false,
                        )
                        DialogButton(
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

@Composable
private fun DialogButton(
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
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, tween(120), label = "dialogScale")

    if (filled) {
        Button(
            onClick = onClick,
            modifier = modifier.scale(scale).height(52.dp),
            enabled = enabled,
            shape = MaterialTheme.shapes.medium,
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        ) {
            Text(text, fontWeight = FontWeight.Medium)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.scale(scale).height(52.dp),
            enabled = enabled,
            shape = MaterialTheme.shapes.medium,
            interactionSource = interactionSource,
            border = BorderStroke(0.5.dp, borderColor),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor, contentColor = contentColor),
        ) {
            Text(text, fontWeight = FontWeight.Medium)
        }
    }
}

private fun createTempImageUri(context: Context): Uri {
    val tempFile = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile,
    )
}
