package com.freight24.connect.feature.trucks.domain.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.auth.AuthDataStore
import com.freight24.common.util.FileUtils
import com.freight24.connect.feature.trucks.domain.usecases.AddTruckParams
import com.freight24.connect.feature.trucks.domain.usecases.AddTruckUseCase
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

data class TruckType(val id: Int, val name: String)

private const val TAG = "OnboardTruckVM"

@HiltViewModel
class OnboardTruckViewModel
    @Inject
    constructor(
        private val addTruckUseCase: AddTruckUseCase,
        private val getTruckOwnersUseCase: GetTruckOwnersUseCase,
        private val checkTruckAvailabilityUseCase: com.freight24.connect.feature.trucks.domain.usecases.CheckTruckAvailabilityUseCase,
        private val authDataStore: AuthDataStore,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState<AddTruckDto>>(UiState.Idle)
        val uiState: StateFlow<UiState<AddTruckDto>> = _uiState.asStateFlow()

        private val _currentStep = MutableStateFlow(1)
        val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

        // Form State
        val registrationNumber = MutableStateFlow("")
        val selectedTruckType = MutableStateFlow<TruckType?>(null)
        val make = MutableStateFlow("")
        val model = MutableStateFlow("")
        val year = MutableStateFlow("")
        val capacityMt = MutableStateFlow("")

        private val _registrationError = MutableStateFlow<String?>(null)
        val registrationError: StateFlow<String?> = _registrationError.asStateFlow()

        // Truck Owner State
        private val _truckOwners = MutableStateFlow<List<TruckOwnerDto>>(emptyList())
        val truckOwnerQuery = MutableStateFlow("")
        val selectedTruckOwner = MutableStateFlow<TruckOwnerDto?>(null)

        val truckOwnerSuggestions: StateFlow<List<TruckOwnerDto>> =
            combine(
                _truckOwners,
                truckOwnerQuery,
                selectedTruckOwner,
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
                if (it.isBlank()) return@map true
                if (it.length != 4 || !it.all { char -> char.isDigit() }) return@map false
                val yearsInt = it.toIntOrNull() ?: 0
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                yearsInt in 2000..currentYear
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

        val isCapacityValid: StateFlow<Boolean> =
            capacityMt.map { (it.toDoubleOrNull() ?: 0.0) > 0.0 }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

        val isOwnerValid: StateFlow<Boolean> =
            selectedTruckOwner.map { it != null }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

        val isStep1Valid: StateFlow<Boolean> =
            combine(
                isRegistrationValid,
                selectedTruckType,
                isYearValid,
                isOwnerValid,
                registrationError,
                isCapacityValid,
            ) { args: Array<Any?> ->
                val reg = args[0] as Boolean
                val type = args[1] as? TruckType
                val yr = args[2] as Boolean
                val owner = args[3] as Boolean
                val regErr = args[4] as? String
                val capacity = args[5] as Boolean
                reg && type != null && yr && owner && regErr == null && capacity
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

        val isStep2Valid: StateFlow<Boolean> =
            rcCardFile.map { it != null }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

        val truckTypes =
            listOf(
                TruckType(15, "20 Feet Closed"),
                TruckType(16, "22 Feet Closed"),
                TruckType(17, "24 Feet Closed"),
                TruckType(10, "32 Feet Single Axle"),
                TruckType(11, "32 Feet Single Axle High Cube"),
                TruckType(12, "32 Feet Multi Axle"),
                TruckType(13, "32 Feet Multi Axle High Cube"),
                TruckType(14, "32 Feet Triple Axle"),
                TruckType(1, "17 Feet Open"),
                TruckType(2, "20 Feet Open"),
                TruckType(3, "22 Feet Open"),
                TruckType(4, "24 Feet Open"),
                TruckType(5, "10 Whl Open"),
                TruckType(6, "12 Whl Open"),
                TruckType(7, "14 Whl Open"),
                TruckType(8, "16 Whl Open"),
                TruckType(9, "18 Whl Open"),
            )

        init {
            loadTruckOwners()
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
            uris: List<Uri>,
            type: String,
            isPdf: Boolean,
            onSuccess: () -> Unit,
        ) {
            if (uris.isEmpty()) return
            Log.d(TAG, "File selected: $type, uris size: ${uris.size}")
            viewModelScope.launch {
                try {
                    val file =
                        if (isPdf) {
                            FileUtils.getFileFromUri(context, uris.first())
                        } else {
                            FileUtils.convertImageToPdf(context, uris)
                        }
                    if (file != null) {
                        Log.d(TAG, "File processed successfully: ${file.absolutePath}")
                        when (type) {
                            "RC" -> rcCardFile.value = file
                            "FC" -> fcCertificateFile.value = file
                            "INSURANCE" -> insuranceFile.value = file
                        }
                        onSuccess()
                    } else {
                        Log.e(TAG, "Failed to process file for $type")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing file", e)
                }
            }
        }

        fun submit() {
            val typeId = selectedTruckType.value?.id ?: return
            val ownerId = selectedTruckOwner.value?.id ?: return
            Log.d(TAG, "Submitting truck: reg=${registrationNumber.value}, type=$typeId, owner=$ownerId")

            viewModelScope.launch {
                _uiState.value = UiState.Loading

                val params =
                    AddTruckParams(
                        registrationNumber = registrationNumber.value.replace(" ", "").uppercase(),
                        truckTypeId = typeId,
                        truckOwnerId = ownerId,
                        make = make.value.ifBlank { null },
                        model = model.value.ifBlank { null },
                        year = year.value.ifBlank { null },
                        capacityMt = capacityMt.value.ifBlank { null },
                        rcCard = rcCardFile.value,
                        fcCertificate = fcCertificateFile.value,
                        insurance = insuranceFile.value,
                    )

                addTruckUseCase(params)
                    .onSuccess { response ->
                        val data = response.data
                        if (response.success && data != null) {
                            Log.d(TAG, "Truck added successfully: ${data.id}")
                            _uiState.value = UiState.Success(data)
                        } else {
                            Log.w(TAG, "Failed to add truck: ${response.message}")
                            _uiState.value = UiState.Error(response.message)
                        }
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Error adding truck", error)
                        _uiState.value = UiState.Error(error.message ?: "Failed to add truck")
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
                        if (existingId != null) {
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
            capacityMt.value = ""
            truckOwnerQuery.value = ""
            selectedTruckOwner.value = null
            rcCardFile.value = null
            fcCertificateFile.value = null
            insuranceFile.value = null
            _registrationError.value = null
        }
    }
