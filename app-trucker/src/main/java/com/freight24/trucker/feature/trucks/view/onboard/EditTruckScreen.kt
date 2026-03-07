package com.freight24.trucker.feature.trucks.view.onboard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.freight24.rest.response.AddTruckDto
import com.freight24.ui.UiState
import com.freight24.ui.components.AppDropdown
import com.freight24.ui.components.DashedUploadCard
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "EditTruckScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditTruckScreen(
    onBackClick: () -> Unit,
    onSuccessAction: () -> Unit,
    navController: NavController,
    viewModel: EditTruckViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val isStep1Valid by viewModel.isStep1Valid.collectAsState()
    val isStep2Valid by viewModel.isStep2Valid.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(currentStep) {
        Log.d(TAG, "Screen step update: $currentStep")
    }

    if (uiState is UiState.Success) {
        EditSuccessScreen(
            truck = (uiState as UiState.Success<AddTruckDto>).data,
            onViewFleet = {
                viewModel.resetState()
                // Set refresh signal before popping back
                navController.previousBackStackEntry?.savedStateHandle?.set("refresh_trucks", true)
                onSuccessAction()
            },
            onAddAnother = { viewModel.resetState() },
            truckTypes = viewModel.truckTypes,
        )
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (currentStep == 1) "Edit Truck Details" else "Edit Documents",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { if (currentStep == 1) onBackClick() else viewModel.previousStep() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            },
            bottomBar = {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp, bottom = 24.dp),
                    // Moved up slightly
                    color = Color.Transparent,
                ) {
                    if (currentStep == 1) {
                        Button(
                            onClick = { viewModel.nextStep() },
                            enabled = isStep1Valid && uiState !is UiState.Loading,
                            shape = RoundedCornerShape(50.dp),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Next", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.previousStep() },
                                enabled = uiState !is UiState.Loading,
                                shape = RoundedCornerShape(50.dp),
                                modifier =
                                    Modifier
                                        .weight(0.4f)
                                        .height(56.dp),
                            ) {
                                Text("Back", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.submit() },
                                enabled = isStep2Valid && uiState !is UiState.Loading,
                                shape = RoundedCornerShape(50.dp),
                                modifier =
                                    Modifier
                                        .weight(0.6f)
                                        .height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (uiState is UiState.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp,
                                        )
                                    } else {
                                        Text("Save Changes", fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.width(8.dp))
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    StepIndicator(currentStep)

                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "StepTransition",
                    ) { step ->
                        when (step) {
                            1 -> BasicInfoStep(viewModel)
                            2 -> DocumentsStep(viewModel)
                        }
                    }
                }

                if (uiState is UiState.Loading) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                if (uiState is UiState.Error) {
                    val errorMessage = (uiState as UiState.Error).message
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .align(Alignment.BottomCenter)
                                .offset(y = (-100).dp), // Move away from bottom bar
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Step $currentStep of 2",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (currentStep == 1) "Basic Info" else "Documents",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(2) { index ->
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                color = if (index + 1 <= currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(2.dp),
                            ),
                )
            }
        }
    }
}

@Composable
private fun BasicInfoStep(viewModel: EditTruckViewModel) {
    val registrationNumber by viewModel.registrationNumber.collectAsState()
    val selectedTruckType by viewModel.selectedTruckType.collectAsState()
    val make by viewModel.make.collectAsState()
    val model by viewModel.model.collectAsState()
    val year by viewModel.year.collectAsState()

    val isRegistrationValid by viewModel.isRegistrationValid.collectAsState()
    val isYearValid by viewModel.isYearValid.collectAsState()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val makeFocusRequester = remember { FocusRequester() }
    val truckTypeFocusRequester = remember { FocusRequester() }
    val yearFocusRequester = remember { FocusRequester() }

    // Auto-focus logic for Registration Number -> Vehicle Type Dropdown
    LaunchedEffect(isRegistrationValid) {
        if (isRegistrationValid) {
            Log.d(TAG, "Registration valid, shifting focus to Vehicle Type Dropdown")
            truckTypeFocusRequester.requestFocus()
        }
    }

    // Keyboard dismissal for Year
    LaunchedEffect(isYearValid) {
        if (isYearValid) {
            Log.d(TAG, "Year valid, hiding keyboard")
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val registrationError by viewModel.registrationError.collectAsState()
 
        OutlinedTextField(
            value = registrationNumber,
            onValueChange = { viewModel.updateRegistrationNumber(it) },
            label = { Text("Vehicle Number") },
            placeholder = { Text("KA07AB1287") },
//            leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        viewModel.checkRegistrationAvailability()
                    }
                },
            isError = registrationError != null,
            supportingText = if (registrationError != null) {
                { Text(registrationError!!) }
            } else null,
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Text,
                ),
        )

        AppDropdown(
            label = "Vehicle Type",
            options = viewModel.truckTypes,
            selectedOption = selectedTruckType,
            onOptionSelected = { viewModel.selectedTruckType.value = it },
            optionToString = { it.name },
            modifier = Modifier.fillMaxWidth(),
            focusRequester = truckTypeFocusRequester,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = make,
                onValueChange = { viewModel.make.value = it },
                label = { Text("Make") },
                placeholder = { Text("Volvo") },
                modifier =
                    Modifier
                        .weight(1f)
                        .focusRequester(makeFocusRequester),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )
            OutlinedTextField(
                value = model,
                onValueChange = { viewModel.model.value = it },
                label = { Text("Model") },
                placeholder = { Text("FH Aero") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )
        }

        OutlinedTextField(
            value = year,
            onValueChange = { viewModel.updateYear(it) },
            label = { Text("Year") },
            placeholder = { Text("2024") },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .focusRequester(yearFocusRequester),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentsStep(viewModel: EditTruckViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val rcFile by viewModel.rcCardFile.collectAsState()
    val fcFile by viewModel.fcCertificateFile.collectAsState()
    val insuranceFile by viewModel.insuranceFile.collectAsState()
 
    val existingRcStatus by viewModel.existingRcStatus.collectAsState()
    val existingFcStatus by viewModel.existingFcStatus.collectAsState()
    val existingInsuranceStatus by viewModel.existingInsuranceStatus.collectAsState()
 
    var showSourcePicker by remember { mutableStateOf(false) }
    var currentTargetPrefix by remember { mutableStateOf<String?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val sheetState = rememberModalBottomSheetState()

    // 1. Gallery Launcher (Photo Picker)
    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            Log.d(TAG, "Gallery result: $uri for $currentTargetPrefix")
            uri?.let { currentTargetPrefix?.let { prefix -> viewModel.onFileSelected(context, it, prefix) } }
            currentTargetPrefix = null
        }

    // 2. Document Launcher (PDF)
    val docLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            Log.d(TAG, "Document result: $uri for $currentTargetPrefix")
            uri?.let { currentTargetPrefix?.let { prefix -> viewModel.onFileSelected(context, it, prefix) } }
            currentTargetPrefix = null
        }

    // 3. Camera Launcher
    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture(),
        ) { success ->
            Log.d(TAG, "Camera result: $success for $currentTargetPrefix")
            if (success) {
                tempCameraUri?.let { currentTargetPrefix?.let { prefix -> viewModel.onFileSelected(context, it, prefix) } }
            }
            currentTargetPrefix = null
            tempCameraUri = null
        }

    // 4. Camera Permission Launcher
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                val uri = createTempImageUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } else {
                Log.w(TAG, "Camera permission denied")
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

    if (showSourcePicker) {
        ModalBottomSheet(
            onDismissRequest = { showSourcePicker = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
            ) {
                Text(
                    text = "Select source",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                )

                ListItem(
                    headlineContent = { Text("Camera") },
                    leadingContent = {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape) {
                            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.padding(8.dp).size(20.dp))
                        }
                    },
                    modifier =
                        Modifier.clickable {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showSourcePicker = false
                                initiateCapture()
                            }
                        },
                )
                ListItem(
                    headlineContent = { Text("Gallery (Images)") },
                    leadingContent = {
                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape) {
                            Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.padding(8.dp).size(20.dp))
                        }
                    },
                    modifier =
                        Modifier.clickable {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showSourcePicker = false
                                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        },
                )
                ListItem(
                    headlineContent = { Text("Documents (PDF)") },
                    leadingContent = {
                        Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = CircleShape) {
                            Icon(Icons.AutoMirrored.Filled.InsertDriveFile, null, modifier = Modifier.padding(8.dp).size(20.dp))
                        }
                    },
                    modifier =
                        Modifier.clickable {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showSourcePicker = false
                                docLauncher.launch(arrayOf("application/pdf"))
                            }
                        },
                )
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = "Upload Documents",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Choose your preferred method to upload mandatory Registration Certificate. Insurance and Fitness are optional.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        DashedUploadCard(
            title = "Registration Certificate",
            subtitle = "Camera, JPG or PDF",
            label = "Required",
            isUploaded = rcFile != null || existingRcStatus != null,
            statusText = when {
                rcFile != null -> "Ready to Upload"
                existingRcStatus != null -> "Existing Document (${existingRcStatus?.lowercase()?.replaceFirstChar { it.uppercase() }})"
                else -> "Pending Selection"
            },
            onUploadClick = {
                currentTargetPrefix = "RC"
                showSourcePicker = true
            },
        )

        DashedUploadCard(
            title = "Insurance Policy",
            subtitle = "Camera, JPG or PDF",
            label = "Optional",
            isUploaded = insuranceFile != null || existingInsuranceStatus != null,
            statusText = when {
                insuranceFile != null -> "Ready to Upload"
                existingInsuranceStatus != null -> "Existing Document (${existingInsuranceStatus?.lowercase()?.replaceFirstChar { it.uppercase() }})"
                else -> null
            },
            icon = Icons.Default.Shield,
            onUploadClick = {
                currentTargetPrefix = "INSURANCE"
                showSourcePicker = true
            },
        )

        DashedUploadCard(
            title = "Fitness Certificate",
            subtitle = "Camera, JPG or PDF",
            label = "Optional",
            isUploaded = fcFile != null || existingFcStatus != null,
            statusText = when {
                fcFile != null -> "Ready to Upload"
                existingFcStatus != null -> "Existing Document (${existingFcStatus?.lowercase()?.replaceFirstChar { it.uppercase() }})"
                else -> null
            },
            icon = Icons.AutoMirrored.Filled.Assignment,
            onUploadClick = {
                currentTargetPrefix = "FC"
                showSourcePicker = true
            },
        )
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

@Composable
private fun EditSuccessScreen(
    truck: AddTruckDto,
    onViewFleet: () -> Unit,
    onAddAnother: () -> Unit,
    truckTypes: List<TruckType>,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Truck Icon with Check Circle
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.size(120.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.size(40.dp).offset(x = 4.dp, y = 4.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Truck Updated Successfully!",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Your vehicle details have been submitted and added to your fleet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(Modifier.height(48.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Truck #${truck.registration_number}", fontWeight = FontWeight.Bold)
                    Text(text = truckTypes[(truck.truck_type_id) - 1].name, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text = "● ACTIVE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(48.dp).alpha(0.3f))
            }
        }

        Spacer(Modifier.height(64.dp))

        Button(
            onClick = onViewFleet,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
        ) {
            Text("View My Fleet", fontWeight = FontWeight.Bold)
        }

        TextButton(
            onClick = onAddAnother,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Add Another Truck", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
