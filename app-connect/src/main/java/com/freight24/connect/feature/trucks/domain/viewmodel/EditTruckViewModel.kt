package com.freight24.connect.feature.trucks.domain.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.auth.AuthDataStore
import androidx.lifecycle.SavedStateHandle
import com.freight24.connect.feature.trucks.domain.usecases.GetTruckByIdUseCase
import com.freight24.connect.feature.trucks.domain.usecases.GetTruckByIdUseCaseParams
import com.freight24.connect.feature.trucks.domain.usecases.UpdateTruckParams
import com.freight24.connect.feature.trucks.domain.usecases.UpdateTruckUseCase
import com.freight24.connect.feature.trucks.domain.usecases.GetTruckOwnersUseCase
import com.freight24.rest.response.AddTruckDto
import com.freight24.rest.response.TruckOwnerDto
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject



private const val TAG = "EditTruckVM"

@HiltViewModel
class EditTruckViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val updateTruckUseCase: UpdateTruckUseCase,
    private val getTruckByIdUseCase: GetTruckByIdUseCase,
    private val checkTruckAvailabilityUseCase: com.freight24.connect.feature.trucks.domain.usecases.CheckTruckAvailabilityUseCase,
    private val getTruckOwnersUseCase: GetTruckOwnersUseCase,
    private val authDataStore: AuthDataStore
) : ViewModel() {
    private val truckId: String = savedStateHandle.get<String>("truckId") ?: ""
 
    private data class InitialData(
        val registrationNumber: String,
        val truckType: TruckType?,
        val make: String,
        val model: String,
        val year: String
    )
 
    private val initialData = MutableStateFlow<InitialData?>(null)

    private val _uiState = MutableStateFlow<UiState<AddTruckDto>>(UiState.Loading)
    val uiState: StateFlow<UiState<AddTruckDto>> = _uiState.asStateFlow()

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    // Form State
    val registrationNumber = MutableStateFlow("")
    val selectedTruckType = MutableStateFlow<TruckType?>(null)
    val make = MutableStateFlow("")
    val model = MutableStateFlow("")
    val year = MutableStateFlow("")
 
    private val _registrationError = MutableStateFlow<String?>(null)
    val registrationError: StateFlow<String?> = _registrationError.asStateFlow()

    // Existing Doc Status
    val existingRcStatus = MutableStateFlow<String?>(null)
    val existingFcStatus = MutableStateFlow<String?>(null)
    val existingInsuranceStatus = MutableStateFlow<String?>(null)

    // Truck Owner State
    private val _truckOwners = MutableStateFlow<List<TruckOwnerDto>>(emptyList())
    val truckOwnerQuery = MutableStateFlow("")
    val selectedTruckOwner = MutableStateFlow<TruckOwnerDto?>(null)

    val truckOwnerSuggestions: StateFlow<List<TruckOwnerDto>> = combine(
        _truckOwners,
        truckOwnerQuery,
        selectedTruckOwner
    ) { owners, query, selected ->
        // If an owner is already selected and the query matches their name, don't show suggestions
        if (selected != null && query == selected.full_name) {
            emptyList()
        } else if (query.isBlank()) {
            emptyList()
        } else {
            owners.filter { 
                it.full_name.contains(query, ignoreCase = true) || 
                it.phone.contains(query) 
            }.take(5)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // File State
    val rcCardFile = MutableStateFlow<File?>(null)
    val fcCertificateFile = MutableStateFlow<File?>(null)
    val insuranceFile = MutableStateFlow<File?>(null)

    // Validation Regex (Modern Indian Plates: e.g. KA 01 AB 1234)
    private val registrationRegex = Regex("^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$")

    val isRegistrationValid: StateFlow<Boolean> =
        registrationNumber.map {
            val cleaned = it.replace(" ", "").uppercase()
            registrationRegex.matches(cleaned)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val isYearValid: StateFlow<Boolean> =
        year.map {
            if (it.length != 4 || !it.all { char -> char.isDigit() }) return@map false
            val yearsInt = it.toIntOrNull() ?: 0
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            yearsInt in 2000..currentYear
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val isOwnerValid: StateFlow<Boolean> =
        selectedTruckOwner.map { it != null }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val isStep1Valid: StateFlow<Boolean> =
        combine(
            isRegistrationValid,
            selectedTruckType,
            make,
            model,
            isYearValid,
            registrationError
        ) { args: Array<Any?> ->
            val reg = args[0] as Boolean
            val type = args[1] as? TruckType
            val mk = args[2] as String
            val md = args[3] as String
            val yr = args[4] as Boolean
            val regErr = args[5] as? String
            reg && type != null && mk.isNotBlank() && md.isNotBlank() && yr && regErr == null
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
 
    val isDirty: StateFlow<Boolean> =
        combine(
            registrationNumber,
            selectedTruckType,
            make,
            model,
            year,
            rcCardFile,
            fcCertificateFile,
            insuranceFile,
            initialData
        ) { args ->
            val reg = args[0] as String
            val type = args[1] as? TruckType
            val mk = args[2] as String
            val md = args[3] as String
            val yr = args[4] as String
            val rcFile = args[5] as? File
            val fcFile = args[6] as? File
            val insFile = args[7] as? File
            val initial = args[8] as? InitialData
            val regErr = args[9] as? String
 
            if (initial == null) return@combine false
            if (regErr != null) return@combine false
 
            val textChanged = reg != initial.registrationNumber ||
                    type != initial.truckType ||
                    mk != initial.make ||
                    md != initial.model ||
                    yr != initial.year
 
            val filesChanged = rcFile != null || fcFile != null || insFile != null
 
            textChanged || filesChanged
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
 
    val isStep2Valid: StateFlow<Boolean> = isDirty
 
    override fun onCleared() {
        super.onCleared()
    }

    val truckTypes =
        listOf(
            TruckType(1, "17 Feet Open"),
            TruckType(2, "20 Feet Open"),
            TruckType(3, "22 Feet Open"),
            TruckType(4, "24 Feet Open"),
            TruckType(5, "10 Whl Open"),
            TruckType(6, "12 Whl Open"),
            TruckType(7, "14 Whl Open"),
            TruckType(8, "16 Whl Open"),
            TruckType(9, "18 Whl Open"),
            TruckType(10, "32 Feet Single Axle"),
            TruckType(11, "32 Feet Single Axle High Cube"),
            TruckType(12, "32 Feet Multi Axle"),
            TruckType(13, "32 Feet Multi Axle High Cube"),
            TruckType(14, "32 Feet Triple Axle"),
            TruckType(15, "20 Feet Closed"),
            TruckType(16, "22 Feet Closed"),
            TruckType(17, "24 Feet Closed"),
        )

    init {
        loadTruckOwners()
        fetchTruckDetails()
    }

    private fun fetchTruckDetails() {
        if (truckId.isEmpty()) {
            _uiState.value = UiState.Error("Truck ID is missing")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getTruckByIdUseCase(GetTruckByIdUseCaseParams(truckId))
                .onSuccess { response ->
                    response.data?.let { truck ->
                        registrationNumber.value = truck.registrationNumber
                        make.value = truck.make ?: ""
                        model.value = truck.model ?: ""
                        year.value = truck.year ?: ""
                        selectedTruckType.value = truckTypes.find { it.name == truck.truckType }
                        
                        existingRcStatus.value = truck.rcCard?.status
                        existingFcStatus.value = truck.fcCertificate?.status
                        existingInsuranceStatus.value = truck.insurance?.status
 
                        initialData.value = InitialData(
                            registrationNumber = truck.registrationNumber,
                            truckType = selectedTruckType.value,
                            make = truck.make ?: "",
                            model = truck.model ?: "",
                            year = truck.year ?: ""
                        )
                        
                        _uiState.value = UiState.Idle
                    } ?: run {
                        _uiState.value = UiState.Error("Truck data not found")
                    }
                }
                .onFailure {
                    _uiState.value = UiState.Error("Failed to fetch truck details")
                }
        }
    }

    private fun loadTruckOwners() {
        viewModelScope.launch {
            getTruckOwnersUseCase()
                .onSuccess { owners ->
                    _truckOwners.value = owners
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to load truck owners", error)
                }
        }
    }

    fun onTruckOwnerSelected(owner: TruckOwnerDto) {
        selectedTruckOwner.value = owner
        truckOwnerQuery.value = owner.full_name
    }

    fun nextStep() {
        if (_currentStep.value < 2) {
            _currentStep.value++
            Log.d(TAG, "Moving to step ${_currentStep.value}")
        }
    }

    fun previousStep() {
        if (_currentStep.value > 1) {
            _currentStep.value--
            Log.d(TAG, "Moving to step ${_currentStep.value}")
        }
    }

    fun onFileSelected(
        context: Context,
        uri: Uri,
        type: String,
    ) {
        Log.d(TAG, "File selected: $type, uri: $uri")
        viewModelScope.launch {
            val file = fromUri(context, uri, type)
            if (file != null) {
                Log.d(TAG, "File processed successfully: ${file.absolutePath}")
            } else {
                Log.e(TAG, "Failed to process file for $type")
            }
            when (type) {
                "RC" -> rcCardFile.value = file
                "FC" -> fcCertificateFile.value = file
                "INSURANCE" -> insuranceFile.value = file
            }
        }
    }

    private fun fromUri(
        context: Context,
        uri: Uri,
        prefix: String,
    ): File? {
        return try {
            val contentResolver = context.contentResolver
            val extension = contentResolver.getType(uri)?.split("/")?.lastOrNull() ?: "pdf"
            val fileName = "${prefix}_${System.currentTimeMillis()}.$extension"
            val tempFile = File(context.cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { input: java.io.InputStream ->
                tempFile.outputStream().use { output: java.io.OutputStream ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error in fromUri", e)
            null
        }
    }

    fun submit() {
        val typeId = selectedTruckType.value?.id ?: return
        Log.d(TAG, "Submitting truck edit: reg=${registrationNumber.value}, type=$typeId")

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            val params =
                UpdateTruckParams(
                    truckId = truckId,
                    registrationNumber = registrationNumber.value.replace(" ", "").uppercase(),
                    truckTypeId = typeId,
                    make = make.value.ifBlank { null },
                    model = model.value.ifBlank { null },
                    year = year.value.ifBlank { null },
                    rcCard = rcCardFile.value,
                    fcCertificate = fcCertificateFile.value,
                    insurance = insuranceFile.value,
                )

            updateTruckUseCase(params)
                .onSuccess { response ->
                    val data = response.data
                    if (response.success && data != null) {
                        Log.d(TAG, "Truck updated successfully: ${data.id}")
                        _uiState.value = UiState.Success(data)
                    } else {
                        Log.w(TAG, "Failed to update truck: ${response.message}")
                        _uiState.value = UiState.Error(response.message)
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "Error updating truck", error)
                    _uiState.value = UiState.Error(error.message ?: "Failed to update truck")
                }
        }
    }

    fun updateRegistrationNumber(newVal: String) {
        val upper = newVal.uppercase().replace(" ", "")
        if (upper.length > 10) return
        _registrationError.value = null // Reset error on change

        val filtered =
            upper.filterIndexed { index, char ->
                when (index) {
                    in 0..1 -> char.isLetter()
                    in 2..3 -> char.isDigit()
                    in 4..5 -> char.isLetter() || char.isDigit()
                    else -> char.isDigit()
                }
            }
        registrationNumber.value = filtered
    }
 
    fun checkRegistrationAvailability() {
        val reg = registrationNumber.value.replace(" ", "").uppercase()
        if (!registrationRegex.matches(reg)) {
            return
        }
 
        viewModelScope.launch {
            checkTruckAvailabilityUseCase(reg)
                .onSuccess { existingId ->
                    // If it belongs to a DIFFERENT truck, it's an error
                    if (existingId != null && existingId != truckId) {
                        _registrationError.value = "Registration number already exists"
                    } else {
                        _registrationError.value = null
                    }
                }
                .onFailure {
                    _registrationError.value = null
                }
        }
    }

    fun updateYear(newVal: String) {
        if (newVal.length > 4) return
        if (newVal.any { !it.isDigit() }) return

        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

        if (newVal.isNotEmpty()) {
            val firstChar = newVal[0]
            if (firstChar != '2') return
        }
        if (newVal.length >= 2) {
            val secondChar = newVal[1]
            if (secondChar != '0') return
        }
        if (newVal.length == 4) {
            val yearsInt = newVal.toIntOrNull() ?: 0
            if (yearsInt !in 2000..currentYear) return
        }

        year.value = newVal
    }

    fun resetState() {
        Log.d(TAG, "Resetting state")
        _uiState.value = UiState.Idle
        _currentStep.value = 1
        registrationNumber.value = ""
        selectedTruckType.value = null
        make.value = ""
        model.value = ""
        year.value = ""
        truckOwnerQuery.value = ""
        selectedTruckOwner.value = null
        rcCardFile.value = null
        fcCertificateFile.value = null
        insuranceFile.value = null
        existingRcStatus.value = null
        existingFcStatus.value = null
        existingInsuranceStatus.value = null
        initialData.value = null
        _registrationError.value = null
    }
}
