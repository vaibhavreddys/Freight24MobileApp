package com.freight24.trucker.feature.trucks.view.onboard

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.state.FormDraft
import com.freight24.common.util.FileUtils
import com.freight24.rest.response.AddTruckDto
import com.freight24.trucker.feature.trucks.domain.usecase.AddTruckParams
import com.freight24.trucker.feature.trucks.domain.usecase.AddTruckUseCase
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.File
import javax.inject.Inject

@Serializable
data class TruckType(val id: Int, val name: String)

/**
 * The truck-onboarding form as it survives process death. See
 * [FormDraft] — only the typed scalars and the picked truck type are carried
 * across.
 *
 * Uploaded document [File]s are deliberately absent. A path into the app's
 * cache is not a durable reference: the upload may have been cleared, and a
 * draft that came back pointing at a missing file would be worse than one that
 * asks for the document again.
 */
@Serializable
internal data class OnboardTruckDraft(
    val registrationNumber: String = "",
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val capacityMt: String = "",
    val selectedTruckType: TruckType? = null,
    val currentStep: Int = 1,
)

private const val TAG = "OnboardTruckVM"

@HiltViewModel
internal class OnboardTruckViewModel
    @Inject
    constructor(
        private val addTruckUseCase: AddTruckUseCase,
        private val checkTruckAvailabilityUseCase: com.freight24.trucker.feature.trucks.domain.usecase.CheckTruckAvailabilityUseCase,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState<AddTruckDto>>(UiState.Idle)
        val uiState: StateFlow<UiState<AddTruckDto>> = _uiState.asStateFlow()

        private val draft = FormDraft(savedStateHandle, DRAFT_KEY, OnboardTruckDraft.serializer())
        private val restored = draft.restore()

        // Restored before the flows are handed out, so the first frame the screen
        // composes already shows the operator's half-finished truck rather than a
        // blank form they would then overwrite.
        private val _currentStep = MutableStateFlow(restored?.currentStep ?: 1)
        val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

        // Form State
        val registrationNumber = MutableStateFlow(restored?.registrationNumber ?: "")
        val selectedTruckType = MutableStateFlow(restored?.selectedTruckType)
        val make = MutableStateFlow(restored?.make ?: "")
        val model = MutableStateFlow(restored?.model ?: "")
        val year = MutableStateFlow(restored?.year ?: "")
        val capacityMt = MutableStateFlow(restored?.capacityMt ?: "")

        private val _registrationError = MutableStateFlow<String?>(null)
        val registrationError: StateFlow<String?> = _registrationError.asStateFlow()

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
            // One writer for the whole form. Every field is a separate
            // StateFlow here rather than a single state object, so this
            // combines them to avoid a persist call per keystroke per field.
            combine(
                registrationNumber,
                make,
                model,
                year,
                capacityMt,
                selectedTruckType,
                _currentStep,
            ) { values: Array<Any?> ->
                // The vararg overload packs the flows into an array, so the
                // transform takes one parameter rather than seven.
                @Suppress("UNCHECKED_CAST")
                OnboardTruckDraft(
                    registrationNumber = values[0] as String,
                    make = values[1] as String,
                    model = values[2] as String,
                    year = values[3] as String,
                    capacityMt = values[4] as String,
                    selectedTruckType = values[5] as TruckType?,
                    currentStep = values[6] as Int,
                )
            }.onEach { draft.save(it) }
                .launchIn(viewModelScope)
        }

        private companion object {
            const val DRAFT_KEY = "trucker_onboard_truck_draft"
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
            Log.d(TAG, "Submitting truck: reg=${registrationNumber.value}, type=$typeId")

            viewModelScope.launch {
                _uiState.value = UiState.Loading
                val params =
                    AddTruckParams(
                        registrationNumber = registrationNumber.value.replace(" ", "").uppercase(),
                        truckTypeId = typeId,
                        make = make.value.ifBlank { null },
                        model = model.value.ifBlank { null },
                        year = year.value.ifBlank { null },
                        capacityMt = capacityMt.value,
                        rcCard = rcCardFile.value,
                        fcCertificate = fcCertificateFile.value,
                        insurance = insuranceFile.value,
                    )

                addTruckUseCase(params)
                    .onSuccess { response ->
                        val data = response.data
                        if (response.success && data != null) {
                            Log.d(TAG, "Truck added successfully: ${data.id}")
                            // The truck exists now, so the draft is spent.
                            // Retired first, or the state update below would
                            // write it straight back and the next onboard would
                            // open pre-filled with this truck's details.
                            draft.clear()
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

            // Strict filtering based on typical Indian plate patterns
            // ^[A-Z]{2}[0-9]{2}[A-Z]{0,2}[0-9]{4}$
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

            // Stricter range-based filtering as they type
            if (newVal.isNotEmpty()) {
                val firstChar = newVal[0]
                if (firstChar != '2') return // We only care about 2000+
            }
            if (newVal.length >= 2) {
                val secondChar = newVal[1]
                if (secondChar != '0') return // Still only 2000-2099
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
            _registrationError.value = null
        }
    }
