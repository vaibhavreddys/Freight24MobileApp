package com.freight24.connect.feature.truckowners.domain.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.truckowners.domain.usecases.CreateTruckOwnerUseCase
import com.freight24.connect.feature.truckowners.domain.usecases.TruckOwnerBankParams
import com.freight24.connect.feature.truckowners.domain.usecases.TruckOwnerBusinessParams
import com.freight24.connect.feature.truckowners.domain.usecases.TruckOwnerPersonalParams
import com.freight24.connect.feature.truckowners.profile.domain.PreferredRoute
import com.freight24.connect.feature.truckowners.profile.util.ProfileFieldValidators
import com.freight24.connect.feature.truckowners.profile.util.ValidationResult
import com.freight24.common.util.FileUtils
import com.freight24.connect.repository.LocationRepository
import com.freight24.connect.feature.trucks.domain.usecases.ValidateTruckOwnerParams
import com.freight24.connect.feature.trucks.domain.usecases.ValidateTruckOwnerUseCase
import com.freight24.rest.request.PreferredRouteEntry
import com.freight24.rest.response.CityResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

internal data class StagedRoute(
    val tempId: String,
    val sourceCityId: Int,
    val sourceCityName: String,
    val destCityId: Int,
    val destCityName: String,
    val priority: Int,
)

internal data class DisplayRoute(
    val id: String,
    val sourceCityId: Int,
    val sourceCityName: String,
    val destCityId: Int,
    val destCityName: String,
    val priority: Int,
    val isStaged: Boolean,
)

@HiltViewModel
class OnboardTruckOwnerViewModel
    @Inject
    constructor(
        private val createTruckOwnerUseCase: CreateTruckOwnerUseCase,
        private val locationRepository: LocationRepository,
        private val validateTruckOwnerUseCase: ValidateTruckOwnerUseCase,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private companion object {
            const val TAG = "OnboardTruckOwnerVM"
            const val USER_ID_KEY = "truck_owner_onboarding_user_id"
            const val COMPLETED_STEP_KEY = "truck_owner_onboarding_completed_step"
            const val OWNER_NAME_KEY = "truck_owner_onboarding_name"
        }

        val fullName = MutableStateFlow("")
        val email = MutableStateFlow("")
        val phone = MutableStateFlow("")
        val alternativePhone = MutableStateFlow("")
        val panCard = MutableStateFlow("")
        val aadharCard = MutableStateFlow("")
        val businessName = MutableStateFlow("")
        val gstNumber = MutableStateFlow("")
        val isTdsApplicable = MutableStateFlow(true)
        val accountHolderName = MutableStateFlow("")
        val accountNumber = MutableStateFlow("")
        val ifscCode = MutableStateFlow("")
        val upiId = MutableStateFlow("")
        val bankName = MutableStateFlow("")
        val branchName = MutableStateFlow("")
        val aadhaarFile = MutableStateFlow<File?>(null)
        val panFile = MutableStateFlow<File?>(null)
        val gstFile = MutableStateFlow<File?>(null)
        val companyFile = MutableStateFlow<File?>(null)
        val tdsFile = MutableStateFlow<File?>(null)
        val chequeFile = MutableStateFlow<File?>(null)

        private val _completedStep = MutableStateFlow(savedStateHandle[COMPLETED_STEP_KEY] ?: 0)
        val completedStep: StateFlow<Int> = _completedStep.asStateFlow()

        private val _baseCity = MutableStateFlow<CityResult?>(null)
        val baseCity: StateFlow<CityResult?> = _baseCity.asStateFlow()
        val baseCityQuery = MutableStateFlow("")
        private val _baseCitySuggestions = MutableStateFlow<List<CityResult>>(emptyList())
        val baseCitySuggestions: StateFlow<List<CityResult>> = _baseCitySuggestions.asStateFlow()
        private val _baseCitySearchLoading = MutableStateFlow(false)
        val baseCitySearchLoading: StateFlow<Boolean> = _baseCitySearchLoading.asStateFlow()
        private val _baseLocation = MutableStateFlow<String?>(null)
        val baseLocation: StateFlow<String?> = _baseLocation.asStateFlow()
        private val _baseCitySearchSkip = MutableStateFlow<String?>(null)

        private val _committedRoutes = MutableStateFlow<List<PreferredRoute>>(emptyList())
        private val _routesToAdd = MutableStateFlow<List<StagedRoute>>(emptyList())
        private val _routeIdsToDelete = MutableStateFlow<Set<String>>(emptySet())
        internal val visibleRoutes: StateFlow<List<DisplayRoute>> =
            combine(_committedRoutes, _routesToAdd, _routeIdsToDelete) { committed, toAdd, toDelete ->
                committed.filter { it.id !in toDelete }.mapNotNull {
                    val sourceCityId = it.sourceCityId ?: return@mapNotNull null
                    val sourceCityName = it.sourceCityName ?: return@mapNotNull null
                    val destCityId = it.destCityId ?: return@mapNotNull null
                    val destCityName = it.destCityName ?: return@mapNotNull null
                    DisplayRoute(
                        id = it.id,
                        sourceCityId = sourceCityId,
                        sourceCityName = sourceCityName,
                        destCityId = destCityId,
                        destCityName = destCityName,
                        priority = it.priority,
                        isStaged = false,
                    )
                } +
                    toAdd.map {
                        DisplayRoute(
                            id = it.tempId,
                            sourceCityId = it.sourceCityId,
                            sourceCityName = it.sourceCityName,
                            destCityId = it.destCityId,
                            destCityName = it.destCityName,
                            priority = it.priority,
                            isStaged = true,
                        )
                    }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        private val _showAddRouteSheet = MutableStateFlow(false)
        val showAddRouteSheet: StateFlow<Boolean> = _showAddRouteSheet.asStateFlow()
        val fromCityQuery = MutableStateFlow("")
        val toCityQuery = MutableStateFlow("")
        private val _fromCitySuggestions = MutableStateFlow<List<CityResult>>(emptyList())
        val fromCitySuggestions: StateFlow<List<CityResult>> = _fromCitySuggestions.asStateFlow()
        private val _toCitySuggestions = MutableStateFlow<List<CityResult>>(emptyList())
        val toCitySuggestions: StateFlow<List<CityResult>> = _toCitySuggestions.asStateFlow()
        private val _fromCityLoading = MutableStateFlow(false)
        val fromCityLoading: StateFlow<Boolean> = _fromCityLoading.asStateFlow()
        private val _toCityLoading = MutableStateFlow(false)
        val toCityLoading: StateFlow<Boolean> = _toCityLoading.asStateFlow()
        private val _selectedFromCity = MutableStateFlow<CityResult?>(null)
        val selectedFromCity: StateFlow<CityResult?> = _selectedFromCity.asStateFlow()
        private val _selectedToCity = MutableStateFlow<CityResult?>(null)
        val selectedToCity: StateFlow<CityResult?> = _selectedToCity.asStateFlow()
        val selectedPriority = MutableStateFlow("High")
        private val _routeSheetError = MutableStateFlow<String?>(null)
        val routeSheetError: StateFlow<String?> = _routeSheetError.asStateFlow()
        private val _fromCitySearchSkip = MutableStateFlow<String?>(null)
        private val _toCitySearchSkip = MutableStateFlow<String?>(null)

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

        init {
            initCitySearchDebounce()
        }

        private val _messageEvent = MutableSharedFlow<String>()
        val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

        private val _createdTruckOwner = MutableStateFlow<CreatedTruckOwnerState?>(null)
        val createdTruckOwner: StateFlow<CreatedTruckOwnerState?> = _createdTruckOwner.asStateFlow()

        private val _panDuplicateError = MutableStateFlow<String?>(null)
        val panDuplicateError: StateFlow<String?> = _panDuplicateError.asStateFlow()

        private val _gstDuplicateError = MutableStateFlow<String?>(null)
        val gstDuplicateError: StateFlow<String?> = _gstDuplicateError.asStateFlow()

        private val _saveAttempted = MutableStateFlow(false)
        val saveAttempted: StateFlow<Boolean> = _saveAttempted.asStateFlow()

        fun onFullNameChanged(input: String) {
            val filtered =
                input.filter { it.isLetter() || it == ' ' || it == '.' }
                    .replace(Regex("\\s+"), " ")
                    .take(60)
                    .trimStart()
            fullName.value = filtered
        }

        fun onEmailChanged(input: String) {
            val filtered =
                input
                    .lowercase()
                    .filter { it.isLetterOrDigit() || it in "@._+-" }
                    .take(80)

            if (filtered.count { it == '@' } <= 1) {
                email.value = filtered
            }
        }

        fun onPhoneChanged(input: String) {
            phone.value = input.filter { it.isDigit() }.take(10)
        }

        fun onAlternativePhoneChanged(input: String) {
            alternativePhone.value = input.filter { it.isDigit() }.take(10)
        }

        fun onAadhaarChanged(input: String) {
            aadharCard.value = input.filter { it.isDigit() }.take(12)
        }

        fun onPanChanged(input: String) {
            val candidate = input.uppercase().filter { it.isLetterOrDigit() }.take(10)
            if (isPanPrefix(candidate)) {
                panCard.value = candidate
            }
            _panDuplicateError.value = null
        }

        fun onPanFocusLost() {
            val pan = panCard.value.trim().uppercase()
            if (pan.length != 10) return
            viewModelScope.launch(Dispatchers.IO) {
                validateTruckOwnerUseCase(ValidateTruckOwnerParams(pan = pan))
                    .onSuccess { validation ->
                        _panDuplicateError.value =
                            if (validation.pan?.exists == true) {
                                "PAN already registered with another truck owner"
                            } else {
                                null
                            }
                    }
            }
        }

        fun onBusinessNameChanged(input: String) {
            val filtered =
                input.filter { it.isLetterOrDigit() || it in " .,&()-/" }
                    .replace(Regex("\\s+"), " ")
                    .take(80)
                    .trimStart()
            businessName.value = filtered
        }

        fun onGstChanged(input: String) {
            val candidate = input.uppercase().filter { it.isLetterOrDigit() }.take(15)
            if (isGstPrefix(candidate)) {
                gstNumber.value = candidate
            }
            _gstDuplicateError.value = null
        }

        fun onGstFocusLost() {
            val gst = gstNumber.value.trim().uppercase()
            if (gst.length != 15) return
            viewModelScope.launch(Dispatchers.IO) {
                validateTruckOwnerUseCase(ValidateTruckOwnerParams(gst = gst))
                    .onSuccess { validation ->
                        _gstDuplicateError.value =
                            if (validation.gst?.exists == true) {
                                "GST already registered with another truck owner"
                            } else {
                                null
                            }
                    }
            }
        }

        fun onTdsApplicableChanged(value: Boolean) {
            isTdsApplicable.value = value
        }

        fun onAccountHolderNameChanged(input: String) {
            val filtered =
                input.filter { it.isLetterOrDigit() || it in " .,&()-/" }
                    .replace(Regex("\\s+"), " ")
                    .take(80)
                    .trimStart()
            accountHolderName.value = filtered
        }

        fun onAccountNumberChanged(input: String) {
            accountNumber.value = input.filter { it.isDigit() }.take(18)
        }

        fun onIfscChanged(input: String) {
            val candidate = input.uppercase().filter { it.isLetterOrDigit() }.take(11)
            if (isIfscPrefix(candidate)) {
                ifscCode.value = candidate
            }
        }

        fun onUpiChanged(input: String) {
            val candidate = input.lowercase().filter { it.isLetterOrDigit() || it in "._-@" }.take(80)
            if (isUpiPrefix(candidate)) {
                upiId.value = candidate
            }
        }

        fun onBankNameChanged(input: String) {
            val filtered =
                input.filter { it.isLetterOrDigit() || it in " .,&()-/" }
                    .replace(Regex("\\s+"), " ")
                    .take(80)
                    .trimStart()
            bankName.value = filtered
        }

        fun onBranchNameChanged(input: String) {
            val filtered =
                input.filter { it.isLetterOrDigit() || it in " .,&()-/" }
                    .replace(Regex("\\s+"), " ")
                    .take(80)
                    .trimStart()
            branchName.value = filtered
        }

        fun onBaseCityQueryChanged(query: String) {
            baseCityQuery.value = query
            if (query.isBlank()) {
                _baseCitySuggestions.value = emptyList()
                _baseLocation.value = null
            }
            if (_baseCity.value?.city != query) {
                _baseCity.value = null
            }
        }

        fun onBaseCitySelected(city: CityResult) {
            _baseCity.value = city
            baseCityQuery.value = city.city
            _baseCitySuggestions.value = emptyList()
            _baseLocation.value = city.city
            _baseCitySearchSkip.value = city.city
        }

        fun openAddRouteSheet() {
            _selectedFromCity.value = _baseCity.value
            fromCityQuery.value = _baseCity.value?.city.orEmpty()
            _selectedToCity.value = null
            toCityQuery.value = ""
            _fromCitySuggestions.value = emptyList()
            _toCitySuggestions.value = emptyList()
            selectedPriority.value = "High"
            _routeSheetError.value = null
            _showAddRouteSheet.value = true
        }

        fun dismissAddRouteSheet() {
            _showAddRouteSheet.value = false
            _routeSheetError.value = null
        }

        fun onFromCityQueryChanged(query: String) {
            fromCityQuery.value = query
            if (query.isBlank()) {
                _fromCitySuggestions.value = emptyList()
            }
            if (_selectedFromCity.value?.city != query) {
                _selectedFromCity.value = null
            }
        }

        fun onToCityQueryChanged(query: String) {
            toCityQuery.value = query
            if (query.isBlank()) {
                _toCitySuggestions.value = emptyList()
            }
            if (_selectedToCity.value?.city != query) {
                _selectedToCity.value = null
            }
        }

        fun onFromCitySelected(city: CityResult) {
            _selectedFromCity.value = city
            fromCityQuery.value = city.city
            _fromCitySuggestions.value = emptyList()
            _fromCitySearchSkip.value = city.city
        }

        fun onToCitySelected(city: CityResult) {
            _selectedToCity.value = city
            toCityQuery.value = city.city
            _toCitySuggestions.value = emptyList()
            _toCitySearchSkip.value = city.city
        }

        fun onStageRouteAdd() {
            _routeSheetError.value = null
            val from = _selectedFromCity.value ?: return
            val to = _selectedToCity.value ?: return
            if (visibleRoutes.value.any { it.sourceCityId == from.id && it.destCityId == to.id }) {
                _routeSheetError.value = "This route is already in your list"
                return
            }
            _routesToAdd.value = _routesToAdd.value +
                StagedRoute(
                    tempId = UUID.randomUUID().toString(),
                    sourceCityId = from.id,
                    sourceCityName = from.city,
                    destCityId = to.id,
                    destCityName = to.city,
                    priority = selectedPriority.value.toPriorityInt(),
                )
            _showAddRouteSheet.value = false
        }

        fun onStageRouteDelete(
            displayRouteId: String,
            isStaged: Boolean,
        ) {
            if (isStaged) {
                _routesToAdd.value = _routesToAdd.value.filter { it.tempId != displayRouteId }
            } else {
                _routeIdsToDelete.value = _routeIdsToDelete.value + displayRouteId
            }
        }

        fun discardRouteChanges() {
            resetRouteStaging()
            _routeSheetError.value = null
            _showAddRouteSheet.value = false
        }

        private fun initCitySearchDebounce() {
            listOf(
                baseCityQuery to "base",
                fromCityQuery to "from",
                toCityQuery to "to",
            ).forEach { (queryFlow, target) ->
                viewModelScope.launch {
                    queryFlow
                        .debounce(400L)
                        .map { it.trim() }
                        .filter { it.length >= 2 }
                        .distinctUntilChanged()
                        .collect { query ->
                            if (shouldSkipSearch(target, query)) return@collect
                            fetchCitySuggestions(query, target)
                        }
                }
            }
        }

        private fun fetchCitySuggestions(
            query: String,
            target: String,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                val loading =
                    when (target) {
                        "base" -> _baseCitySearchLoading
                        "from" -> _fromCityLoading
                        else -> _toCityLoading
                    }
                loading.value = true
                val type = if (target == "base") null else "CITY"
                locationRepository.searchCities(query, type)
                    .onSuccess { results ->
                        when (target) {
                            "base" -> _baseCitySuggestions.value = results
                            "from" -> _fromCitySuggestions.value = results
                            else -> _toCitySuggestions.value = results
                        }
                    }
                loading.value = false
            }
        }

        private fun shouldSkipSearch(
            target: String,
            query: String,
        ): Boolean {
            val skipFlow =
                when (target) {
                    "base" -> _baseCitySearchSkip
                    "from" -> _fromCitySearchSkip
                    else -> _toCitySearchSkip
                }
            val skipValue = skipFlow.value
            return if (skipValue != null && skipValue == query) {
                skipFlow.value = null
                true
            } else {
                false
            }
        }

        private fun resetRouteStaging() {
            _routesToAdd.value = emptyList()
            _routeIdsToDelete.value = emptySet()
        }

        private fun gatherPreferredRouteEntries(): List<PreferredRouteEntry> {
            val activeCommits = _committedRoutes.value.filter { it.id !in _routeIdsToDelete.value }
            return activeCommits.mapNotNull { it.toEntry() } + _routesToAdd.value.map { it.toEntry() }
        }

        private fun PreferredRoute.toEntry(): PreferredRouteEntry? {
            val sourceId = sourceCityId ?: return null
            val destId = destCityId ?: return null
            return PreferredRouteEntry(
                sourceCityId = sourceId,
                destinationCityId = destId,
                priority = priority,
            )
        }

        private fun StagedRoute.toEntry(): PreferredRouteEntry =
            PreferredRouteEntry(
                sourceCityId = sourceCityId,
                destinationCityId = destCityId,
                priority = priority,
            )

        fun hasChanges(): Boolean {
            return fullName.value.isNotBlank() ||
                email.value.isNotBlank() ||
                phone.value.isNotBlank() ||
                alternativePhone.value.isNotBlank() ||
                panCard.value.isNotBlank() ||
                aadharCard.value.isNotBlank() ||
                businessName.value.isNotBlank() ||
                gstNumber.value.isNotBlank() ||
                accountHolderName.value.isNotBlank() ||
                accountNumber.value.isNotBlank() ||
                ifscCode.value.isNotBlank() ||
                upiId.value.isNotBlank() ||
                bankName.value.isNotBlank() ||
                branchName.value.isNotBlank() ||
                !isTdsApplicable.value ||
                listOf(aadhaarFile, panFile, gstFile, companyFile, tdsFile, chequeFile).any { it.value != null }
        }

        fun onDocumentSelected(
            context: Context,
            uri: Uri,
            type: String,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                val file = FileUtils.getFileFromUri(context, uri)
                if (file == null) {
                    _messageEvent.emit("Failed to process document")
                    return@launch
                }
                when (type) {
                    "AADHAAR" -> aadhaarFile.value = file
                    "PAN" -> panFile.value = file
                    "GST" -> gstFile.value = file
                    "COMPANY" -> companyFile.value = file
                    "TDS" -> tdsFile.value = file
                    "CHEQUE" -> chequeFile.value = file
                }
            }
        }

        fun canSubmit(): Boolean {
            val reason = mandatoryValidationError()
            return reason == null
        }

        fun submitBlockReason(): String? = mandatoryValidationError()

        fun submit() {
            if (_isSaving.value) return
            _saveAttempted.value = true

            val error = mandatoryValidationError()
            if (error != null) {
                Log.w(
                    TAG,
                    "Submit blocked: $error [phoneLen=${phone.value.length}, aadhaarLen=${aadharCard.value.length}, panLen=${panCard.value.length}, accLen=${accountNumber.value.length}, ifscLen=${ifscCode.value.length}]",
                )
                viewModelScope.launch { _messageEvent.emit(error) }
                return
            }

            viewModelScope.launch {
                _isSaving.value = true
                try {
                    var userId: String? = savedStateHandle[USER_ID_KEY]

                    if (_completedStep.value < 1) {
                        val normalizedPhone = phone.value.trim().let { if (it.length == 10) "+91$it" else it }
                        userId = createTruckOwnerUseCase.createPersonal(
                            TruckOwnerPersonalParams(
                                fullName = fullName.value.trim(),
                                email = email.value.trim().ifBlank { null },
                                phone = normalizedPhone,
                                alternativePhone = alternativePhone.value.trim().ifBlank { null },
                                panCard = panCard.value.trim().uppercase(),
                                aadhaarCard = aadharCard.value.trim(),
                                aadhaarFile = requireNotNull(aadhaarFile.value),
                                panFile = requireNotNull(panFile.value),
                            ),
                        ).getOrElse {
                            _messageEvent.emit(it.message ?: "Failed to save personal details")
                            return@launch
                        }
                        savedStateHandle[USER_ID_KEY] = userId
                        savedStateHandle[OWNER_NAME_KEY] = fullName.value.trim()
                        setCompletedStep(1)
                    }

                    val resolvedUserId = requireNotNull(userId)
                    if (_completedStep.value < 2) {
                        createTruckOwnerUseCase.saveBusiness(
                            resolvedUserId,
                            TruckOwnerBusinessParams(
                                businessName = businessName.value.trim().ifBlank { null },
                                gstNumber = gstNumber.value.trim().uppercase().ifBlank { null },
                                isTdsApplicable = isTdsApplicable.value,
                                baseCityId = _baseCity.value?.id,
                                preferredRoutes = gatherPreferredRouteEntries(),
                                gstFile = gstFile.value,
                                companyFile = companyFile.value,
                                tdsFile = tdsFile.value,
                            ),
                        ).getOrElse {
                            _messageEvent.emit("Personal details saved. ${it.message ?: "Failed to save business details"}")
                            return@launch
                        }
                        setCompletedStep(2)
                    }

                    createTruckOwnerUseCase.saveBank(
                        resolvedUserId,
                        TruckOwnerBankParams(
                            accountHolderName = accountHolderName.value.trim(),
                            accountNumber = accountNumber.value.trim(),
                            ifscCode = ifscCode.value.trim().uppercase(),
                            upiId = upiId.value.trim().lowercase().ifBlank { null },
                            bankName = bankName.value.trim().ifBlank { null },
                            branchName = branchName.value.trim().ifBlank { null },
                            chequeFile = requireNotNull(chequeFile.value),
                        ),
                    ).getOrElse {
                        _messageEvent.emit("Profile saved. ${it.message ?: "Failed to save bank details"}")
                        return@launch
                    }

                    val ownerName = (savedStateHandle[OWNER_NAME_KEY] as String?).orEmpty().ifBlank { fullName.value.trim() }
                    _createdTruckOwner.value = CreatedTruckOwnerState(ownerName, resolvedUserId)
                    savedStateHandle.remove<String>(USER_ID_KEY)
                    savedStateHandle.remove<Int>(COMPLETED_STEP_KEY)
                    savedStateHandle.remove<String>(OWNER_NAME_KEY)
                    _completedStep.value = 0
                    _messageEvent.emit("Truck owner created successfully")
                } finally {
                    _isSaving.value = false
                }
            }
        }

        private fun setCompletedStep(step: Int) {
            _completedStep.value = step
            savedStateHandle[COMPLETED_STEP_KEY] = step
        }

        private fun mandatoryValidationError(): String? {
            if (_completedStep.value < 1) {
                if (fullName.value.trim().isBlank()) return "Full name is required"
                if (phone.value.trim().isBlank()) return "Phone number is required"
                if (aadharCard.value.trim().isBlank()) return "Aadhaar number is required"
                if (panCard.value.trim().isBlank()) return "PAN number is required"
                if (aadhaarFile.value == null) return "Aadhaar document is required"
                if (panFile.value == null) return "PAN document is required"
            }
            if (accountHolderName.value.trim().isBlank()) return "Account holder name is required"
            if (accountNumber.value.trim().isBlank()) return "Account number is required"
            if (ifscCode.value.trim().isBlank()) return "IFSC code is required"
            if (chequeFile.value == null) return "Cancelled cheque is required"

            if (_completedStep.value < 1) {
                when (ProfileFieldValidators.validateIndianPhone(phone.value.trim())) {
                    is ValidationResult.Error -> return "Enter a valid primary phone number"
                    ValidationResult.Success -> Unit
                }
            }

            if (aadharCard.value.isNotBlank()) {
                when (val result = ProfileFieldValidators.validateAadhaar(aadharCard.value.trim())) {
                    is ValidationResult.Error -> return result.message
                    ValidationResult.Success -> Unit
                }
            }

            if (panCard.value.isNotBlank()) {
                when (val result = ProfileFieldValidators.validatePan(panCard.value.trim())) {
                    is ValidationResult.Error -> return result.message
                    ValidationResult.Success -> Unit
                }
            }

            if (gstNumber.value.isNotBlank()) {
                when (val result = ProfileFieldValidators.validateGst(gstNumber.value.trim())) {
                    is ValidationResult.Error -> return result.message
                    ValidationResult.Success -> Unit
                }
            }

            if (panCard.value.trim().length == 10 && _panDuplicateError.value != null) {
                return "PAN already registered with another truck owner"
            }

            if (gstNumber.value.trim().length == 15 && _gstDuplicateError.value != null) {
                return "GST already registered with another truck owner"
            }

            return null
        }

        private fun isPanPrefix(value: String): Boolean {
            return value.withIndex().all { (index, c) ->
                when (index) {
                    0, 1, 2, 3, 4, 9 -> c.isLetter()
                    5, 6, 7, 8 -> c.isDigit()
                    else -> false
                }
            }
        }

        private fun isIfscPrefix(value: String): Boolean {
            return value.withIndex().all { (index, c) ->
                when {
                    index in 0..3 -> c.isLetter()
                    index == 4 -> c == '0'
                    else -> c.isLetterOrDigit()
                }
            }
        }

        private fun isGstPrefix(value: String): Boolean {
            return value.withIndex().all { (index, c) ->
                when {
                    index in 0..1 -> c.isDigit()
                    index in 2..6 -> c.isLetter()
                    index in 7..10 -> c.isDigit()
                    index == 11 -> c.isLetter()
                    index == 12 -> c.isLetterOrDigit() && c != '0'
                    index == 13 -> c == 'Z'
                    index == 14 -> c.isLetterOrDigit()
                    else -> false
                }
            }
        }

        private fun isUpiPrefix(value: String): Boolean {
            if (value.count { it == '@' } > 1) return false
            return Regex("^[a-z0-9._-]*(@[a-z0-9.]*)?$").matches(value)
        }

        private fun String.toPriorityInt(): Int =
            when (this) {
                "High" -> 1
                "Medium" -> 2
                else -> 3
            }

        fun consumeCreatedTruckOwner() {
            _createdTruckOwner.value = null
        }

        data class CreatedTruckOwnerState(
            val fullName: String,
            val userId: String?,
        )
    }
