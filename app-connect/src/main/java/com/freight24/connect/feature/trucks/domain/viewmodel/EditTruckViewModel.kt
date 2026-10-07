package com.freight24.connect.feature.trucks.domain.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.auth.AuthDataStore
import com.freight24.common.util.FileUtils
import com.freight24.connect.feature.trucks.domain.usecases.GetTruckByIdUseCase
import com.freight24.connect.feature.trucks.domain.usecases.GetTruckByIdUseCaseParams
import com.freight24.connect.feature.trucks.domain.usecases.UpdateTruckParams
import com.freight24.connect.feature.trucks.domain.usecases.UpdateTruckUseCase
import com.freight24.connect.domain.model.TruckDetails
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
        private val authDataStore: AuthDataStore,
    ) : ViewModel() {
        private val truckId: String = savedStateHandle.get<String>("truckId") ?: ""

        private data class InitialData(
            val registrationNumber: String,
            val truckType: TruckType?,
            val make: String,
            val model: String,
            val year: String,
            val capacityMt: String,
        )

        private val initialData = MutableStateFlow<InitialData?>(null)

        private val _uiState = MutableStateFlow<UiState<TruckDetails>>(UiState.Loading)
        val uiState: StateFlow<UiState<TruckDetails>> = _uiState.asStateFlow()

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

        // Existing Doc Status
        val existingRcStatus = MutableStateFlow<String?>(null)
        val existingFcStatus = MutableStateFlow<String?>(null)
        val existingInsuranceStatus = MutableStateFlow<String?>(null)

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

        val isStep1Valid: StateFlow<Boolean> =
            combine(
                isRegistrationValid,
                selectedTruckType,
                isYearValid,
                registrationError,
                isCapacityValid,
            ) { args: Array<Any?> ->
                val reg = args[0] as Boolean
                val type = args[1] as? TruckType
                val yr = args[2] as Boolean
                val regErr = args[3] as? String
                val capacity = args[4] as Boolean
                reg && type != null && yr && regErr == null && capacity
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

        val isDirty: StateFlow<Boolean> =
            combine(
                registrationNumber,
                selectedTruckType,
                make,
                model,
                year,
                capacityMt,
                rcCardFile,
                fcCertificateFile,
                insuranceFile,
                initialData,
            ) { args ->
                val reg = args[0] as String
                val type = args[1] as? TruckType
                val mk = args[2] as String
                val md = args[3] as String
                val yr = args[4] as String
                val capacity = args[5] as String
                val rcFile = args[6] as? File
                val fcFile = args[7] as? File
                val insFile = args[8] as? File
                val initial = args[9] as? InitialData

                if (initial == null) return@combine false

                val textChanged =
                    reg != initial.registrationNumber ||
                        type != initial.truckType ||
                        mk != initial.make ||
                        md != initial.model ||
                        yr != initial.year ||
                        capacity != initial.capacityMt

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
                    .onSuccess { truck ->
                            registrationNumber.value = truck.registrationNumber
                            make.value = truck.vehicle?.make ?: ""
                            model.value = truck.vehicle?.model ?: ""
                            year.value = truck.vehicle?.manufacturingYear?.toString() ?: ""
                            capacityMt.value = truck.vehicle?.capacityMt?.toString()?.removeSuffix(".0") ?: ""
                            selectedTruckType.value = truckTypes.find { it.id == truck.truckTypeId }
                                ?: truckTypes.find { it.name == truck.truckType }

                            existingRcStatus.value = truck.documents?.registrationCertificate?.status
                            existingFcStatus.value = truck.documents?.fitnessCertificate?.status
                            existingInsuranceStatus.value = truck.documents?.insurance?.status

                            initialData.value =
                                InitialData(
                                    registrationNumber = truck.registrationNumber,
                                    truckType = selectedTruckType.value,
                                    make = truck.vehicle?.make ?: "",
                                    model = truck.vehicle?.model ?: "",
                                    year = truck.vehicle?.manufacturingYear?.toString() ?: "",
                                    capacityMt = truck.vehicle?.capacityMt?.toString()?.removeSuffix(".0") ?: "",
                                )

                            _uiState.value = UiState.Idle
                    }
                    .onFailure { error ->
                        _uiState.value = UiState.Error(error.message ?: "Failed to fetch truck details")
                    }
            }
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
            if (uris.isEmpty() || _uiState.value is UiState.Loading) return
            _uiState.value = UiState.Loading
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
                        _uiState.value = UiState.Error("Failed to process document")
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing file", e)
                    _uiState.value = UiState.Error(e.message ?: "Failed to process document")
                } finally {
                    if (_uiState.value is UiState.Loading) _uiState.value = UiState.Idle
                }
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
                        make = make.value,
                        model = model.value,
                        year = year.value,
                        capacityMt = capacityMt.value,
                        rcCard = rcCardFile.value,
                        fcCertificate = fcCertificateFile.value,
                        insurance = insuranceFile.value,
                    )

                updateTruckUseCase(params)
                    .onSuccess { truck ->
                        Log.d(TAG, "Truck updated successfully: ${truck.truckId}")
                        _uiState.value = UiState.Success(truck)
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
                checkTruckAvailabilityUseCase(reg, excludeTruckId = truckId)
                    .onSuccess { validation ->
                        if (validation.exists) {
                            _registrationError.value = "Registration number already exists"
                        } else {
                            _registrationError.value = null
                        }
                    }
                    .onFailure { error ->
                        _registrationError.value = error.message ?: "Unable to validate registration number"
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
