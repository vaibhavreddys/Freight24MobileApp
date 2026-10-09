package com.freight24.trucker.feature.profile.view

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.util.DocumentUploadPolicy
import com.freight24.common.util.FileUtils
import com.freight24.rest.api.TruckOwnerApi
import com.freight24.rest.repository.LocationRepository
import com.freight24.rest.request.FinalSaveFileRequest
import com.freight24.rest.request.FinalSaveRequest
import com.freight24.rest.request.PreferredRouteEntry
import com.freight24.rest.response.CityResult
import com.freight24.trucker.feature.profile.domain.DocumentType
import com.freight24.trucker.feature.profile.domain.PreferredRoute
import com.freight24.trucker.feature.profile.domain.Profile
import com.freight24.trucker.feature.profile.domain.ProfileRepository
import com.freight24.trucker.feature.profile.domain.usecase.FetchProfileUseCase
import com.freight24.trucker.feature.profile.domain.usecase.FinalizeIdentityDocumentsUseCase
import com.freight24.trucker.feature.profile.domain.usecase.UpdateIdentityDocumentParams
import com.freight24.trucker.feature.profile.domain.usecase.UpdateIdentityDocumentUseCase
import com.freight24.trucker.feature.profile.domain.usecase.UpdateProfileParams
import com.freight24.trucker.feature.profile.domain.usecase.UpdateProfileUseCase
import com.freight24.trucker.feature.profile.domain.usecase.UploadIdentityDocumentParams
import com.freight24.trucker.feature.profile.domain.usecase.UploadIdentityDocumentUseCase
import com.freight24.trucker.feature.profile.util.ProfileDocumentTypeIds
import com.freight24.trucker.feature.profile.util.ProfileFieldValidators
import com.freight24.trucker.feature.profile.util.ValidationResult
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.UUID
import javax.inject.Inject

internal enum class UploadStatus {
    IDLE,
    UPLOADING,
    DONE,
    ERROR,
}

internal data class DocumentUploadState(
    val fieldKey: String,
    val documentTypeId: Int,
    val existingDocId: String?,
    val existingFileUrl: String?,
    val existingVerifyStatus: String?,
    val temporaryFileKey: String?,
    val pendingLocalFile: File? = null,
    val uploadStatus: UploadStatus = UploadStatus.IDLE,
    val isPreparingDocument: Boolean = false,
)

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

/**
 * Document-completion summary derived on the client from upload state.
 *
 * A document counts as [uploaded] once it has either a server-side file or a
 * freshly staged temporary upload — the latter so the meter responds the
 * moment a user picks a file, before they hit Save.
 */
internal data class ProfileCompletion(
    val uploaded: Int,
    val total: Int,
) {
    val missing: Int get() = (total - uploaded).coerceAtLeast(0)
    val isComplete: Boolean get() = total > 0 && missing == 0
    val percent: Int get() = if (total <= 0) 0 else (uploaded * 100) / total
}

@OptIn(FlowPreview::class)
@HiltViewModel
internal class ProfileViewModel
    @Inject
    constructor(
        private val fetchProfileUseCase: FetchProfileUseCase,
        private val updateProfileUseCase: UpdateProfileUseCase,
        private val uploadIdentityDocumentUseCase: UploadIdentityDocumentUseCase,
        private val updateIdentityDocumentUseCase: UpdateIdentityDocumentUseCase,
        private val finalizeIdentityDocumentsUseCase: FinalizeIdentityDocumentsUseCase,
        private val getProfileSignedUrlUseCase: com.freight24.trucker.feature.profile.domain.usecase.GetProfileSignedUrlUseCase,
        private val profileRepository: ProfileRepository,
        private val locationRepository: LocationRepository,
        private val sessionManager: com.freight24.auth.SessionManager,
        private val truckOwnerApi: TruckOwnerApi,
        @param:ApplicationContext private val context: Context,
    ) : ViewModel() {
        val sessionId: String = UUID.randomUUID().toString()

        private val _uiState = MutableStateFlow<UiState<Profile>>(UiState.Loading)
        val uiState: StateFlow<UiState<Profile>> = _uiState.asStateFlow()

        private val _previewingDocumentId = MutableStateFlow<String?>(null)
        val previewingDocumentId: StateFlow<String?> = _previewingDocumentId.asStateFlow()

        private val _isEditing = MutableStateFlow(false)
        val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        private val _bankValidationErrors = MutableStateFlow<Map<String, String>>(emptyMap())
        val bankValidationErrors: StateFlow<Map<String, String>> = _bankValidationErrors.asStateFlow()

        private val _isIfscLookupInProgress = MutableStateFlow(false)
        val isIfscLookupInProgress: StateFlow<Boolean> = _isIfscLookupInProgress.asStateFlow()
        private var ifscLookupJob: Job? = null

        private var originalProfile: Profile? = null

        val editableFullName = MutableStateFlow("")
        val editableEmailId = MutableStateFlow("")
        val editablePhoneNo = MutableStateFlow("")
        val editableAadhaarNumber = MutableStateFlow("")
        val editablePanNumber = MutableStateFlow("")
        val editableAlternativePhone = MutableStateFlow("")
        val editableBusinessName = MutableStateFlow("")
        val editableGstNumber = MutableStateFlow("")
        val editableAccountHolderName = MutableStateFlow("")
        val editableAccountNumber = MutableStateFlow("")
        val editableIfscCode = MutableStateFlow("")
        val editableUpiId = MutableStateFlow("")
        val editableBankName = MutableStateFlow("")
        val editableBranchName = MutableStateFlow("")

        private val _baseCity = MutableStateFlow<CityResult?>(null)
        val baseCity: StateFlow<CityResult?> = _baseCity.asStateFlow()
        private val _baseLocation = MutableStateFlow<String?>(null)
        val baseLocation: StateFlow<String?> = _baseLocation.asStateFlow()

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

        private val _showLogoutSheet = MutableStateFlow(false)
        val showLogoutSheet: StateFlow<Boolean> = _showLogoutSheet.asStateFlow()

        fun requestLogout() {
            _showLogoutSheet.value = true
        }

        fun dismissLogoutSheet() {
            _showLogoutSheet.value = false
        }

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

        private val _documentStates = MutableStateFlow<Map<String, DocumentUploadState>>(emptyMap())
        val documentStates: StateFlow<Map<String, DocumentUploadState>> = _documentStates.asStateFlow()

        /**
         * Document types this partner must provide, TDS already filtered out
         * when it does not apply. Drives both the visible list and the
         * completion denominator.
         */
        private val _documentTypes = MutableStateFlow<List<DocumentType>>(emptyList())
        val documentTypes: StateFlow<List<DocumentType>> = _documentTypes.asStateFlow()

        private val _completion = MutableStateFlow(ProfileCompletion(uploaded = 0, total = 0))
        val completion: StateFlow<ProfileCompletion> = _completion.asStateFlow()

        private val _hasChanges = MutableStateFlow(false)
        val hasChanges: StateFlow<Boolean> = _hasChanges.asStateFlow()

        /** How many edits are staged. Drives the action bar's status line. */
        private val _changeCount = MutableStateFlow(0)
        val changeCount: StateFlow<Int> = _changeCount.asStateFlow()

        private val _hasPendingTemporaryUploads = MutableStateFlow(false)
        val hasPendingTemporaryUploads: StateFlow<Boolean> = _hasPendingTemporaryUploads.asStateFlow()

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

        private val _panDuplicateError = MutableStateFlow<String?>(null)
        val panDuplicateError: StateFlow<String?> = _panDuplicateError.asStateFlow()

        private val _gstDuplicateError = MutableStateFlow<String?>(null)
        val gstDuplicateError: StateFlow<String?> = _gstDuplicateError.asStateFlow()

        private val _uploadEvent = MutableSharedFlow<Result<Unit>>()
        val uploadEvent = _uploadEvent.asSharedFlow()

        private val _saveEvent = MutableSharedFlow<Result<Unit>>()
        val saveEvent = _saveEvent.asSharedFlow()

        init {
            initCitySearchDebounce()
            fetchProfile()
        }

        fun fetchProfile(forceRefresh: Boolean = false) {
            viewModelScope.launch(Dispatchers.IO) {
                if (forceRefresh) {
                    _isRefreshing.value = true
                } else {
                    _uiState.value = UiState.Loading
                }

                fetchProfileUseCase.invoke(com.freight24.common.usecase.EmptyUseCaseParams)
                    .onSuccess { profile ->
                        // An identical payload must not re-seed the form: the side
                        // effects below are not protected by equality, so they
                        // would clear staged edits even though nothing changed.
                        if (profile == originalProfile) return@onSuccess

                        originalProfile = profile
                        populateEditableFields(profile)
                        initializeDocumentStates(profile, preserveTemporary = _isEditing.value)
                        populateBaseCity(profile)
                        _committedRoutes.value = profile.preferredRoutes
                        resetRouteStaging()
                        _uiState.value = UiState.Success(profile)
                        recomputeHasChanges()
                    }
                    .onFailure { error ->
                        // Keep the profile on screen when a silent reload fails;
                        // only a cold start with nothing to show becomes an error.
                        if (forceRefresh && _uiState.value is UiState.Success) return@onFailure
                        _uiState.value = UiState.Error(error.message ?: "Failed to fetch profile")
                    }

                _isRefreshing.value = false
            }
        }

        private fun populateEditableFields(profile: Profile) {
            editableFullName.value = profile.fullName.orEmpty()
            editableEmailId.value = profile.emailId.orEmpty()
            editablePhoneNo.value = profile.phoneNo.orEmpty()
            editableAadhaarNumber.value = profile.aadhaarNumber.orEmpty()
            editablePanNumber.value = profile.panNumber.orEmpty()
            editableAlternativePhone.value = profile.alternativePhoneNo.firstOrNull().orEmpty()
            editableBusinessName.value = profile.businessName.orEmpty()
            editableGstNumber.value = profile.gstNumber.orEmpty()
            editableAccountHolderName.value = profile.bankAccountDetails?.accountHolderName.orEmpty()
            editableAccountNumber.value = profile.bankAccountDetails?.accountNumber.orEmpty()
            editableIfscCode.value = profile.bankAccountDetails?.ifscCode.orEmpty()
            editableUpiId.value = profile.bankAccountDetails?.upiId.orEmpty()
            editableBankName.value = profile.bankAccountDetails?.bankName.orEmpty()
            editableBranchName.value = profile.bankAccountDetails?.branchName.orEmpty()
        }

        private fun initializeDocumentStates(
            profile: Profile,
            preserveTemporary: Boolean,
        ) {
            val previous = _documentStates.value
            val docsByType = profile.documents.associateBy { it.type }

            val applicableTypes = ProfileDocumentTypeIds.applicableDocumentTypes(profile.isTdsApplicable)
            _documentTypes.value = applicableTypes

            val nextStates =
                applicableTypes.associate { type ->
                    val fieldKey = ProfileDocumentTypeIds.fieldKey(type)
                    val existing = docsByType[type]
                    val bank = profile.bankAccountDetails.takeIf { type == DocumentType.CANCELLED_CHEQUE }
                    val oldState = previous[fieldKey]
                    val documentTypeId = ProfileDocumentTypeIds.documentTypeId(type) ?: 0

                    fieldKey to
                        DocumentUploadState(
                            fieldKey = fieldKey,
                            documentTypeId = documentTypeId,
                            existingDocId = bank?.id ?: existing?.docId,
                            existingFileUrl = bank?.chequeImageUrl ?: existing?.url,
                            existingVerifyStatus = bank?.verificationStatus?.lowercase()
                                ?: existing?.let { ProfileDocumentTypeIds.verifyStatus(it.status) },
                            temporaryFileKey = if (preserveTemporary) oldState?.temporaryFileKey else null,
                            pendingLocalFile = if (preserveTemporary) oldState?.pendingLocalFile else null,
                            uploadStatus =
                                if (preserveTemporary && (oldState?.temporaryFileKey != null || oldState?.pendingLocalFile != null)) {
                                    if (oldState.uploadStatus == UploadStatus.ERROR) UploadStatus.ERROR else UploadStatus.DONE
                                } else {
                                    UploadStatus.IDLE
                                },
                        )
                }
            // Drop state for types that are no longer applicable (e.g. TDS was
            // switched off server-side) so they cannot be finalised by accident.
            _documentStates.value = nextStates

            recomputeDocumentDerivedState()
        }

        private fun populateBaseCity(profile: Profile) {
            val baseCityId = profile.baseCityId
            val baseCityName = profile.baseCityName
            val baseLabel = profile.baseLocation?.ifBlank { null } ?: baseCityName
            _baseLocation.value = baseLabel
            if (baseCityId != null && !baseCityName.isNullOrBlank()) {
                _baseCity.value =
                    CityResult(
                        type = "CITY",
                        id = baseCityId,
                        name = baseCityName,
                        city = baseCityName,
                        state = profile.baseCityState.orEmpty(),
                    )
            } else {
                _baseCity.value = null
            }
        }

        private fun initCitySearchDebounce() {
            listOf(
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
                        "from" -> _fromCityLoading
                        else -> _toCityLoading
                    }
                loading.value = true
                locationRepository.searchCities(query, "CITY")
                    .onSuccess { results ->
                        when (target) {
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

        fun onBaseCitySelected(city: CityResult) {
            _baseCity.value = city
            _baseLocation.value = city.name
            recomputeHasChanges()
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
            recomputeHasChanges()
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
            recomputeHasChanges()
        }

        fun discardRouteChanges() {
            resetRouteStaging()
            _routeSheetError.value = null
            _showAddRouteSheet.value = false
        }

        private fun resetRouteStaging() {
            _routesToAdd.value = emptyList()
            _routeIdsToDelete.value = emptySet()
        }

        fun toggleEditMode() {
            if (_isEditing.value) {
                cancelEdit()
            } else {
                _isEditing.value = true
                recomputeHasChanges()
            }
        }

        fun onFieldChanged() {
            recomputeHasChanges()
        }

        fun onPanFocusLost() {
            val pan = editablePanNumber.value.trim().uppercase()
            if (pan.length != 10) return
            val original = originalProfile
            if (original != null && pan == original.panNumber.orEmpty().trim().uppercase()) return
            val excludeId = original?.userId ?: return
            viewModelScope.launch(Dispatchers.IO) {
                val response = truckOwnerApi.checkTruckOwnerField("pan_card", pan, excludeId = excludeId)
                if (response.isSuccessful && response.body()?.exists == true) {
                    _panDuplicateError.value = "PAN already registered with another truck owner"
                } else if (response.isSuccessful) {
                    _panDuplicateError.value = null
                }
            }
        }

        fun onGstFocusLost() {
            val gst = editableGstNumber.value.trim().uppercase()
            if (gst.length != 15) return
            val original = originalProfile
            if (original != null && gst == original.gstNumber.orEmpty().trim().uppercase()) return
            val excludeId = original?.userId ?: return
            viewModelScope.launch(Dispatchers.IO) {
                val response = truckOwnerApi.checkTruckOwnerField("gst_number", gst, excludeId = excludeId)
                if (response.isSuccessful && response.body()?.exists == true) {
                    _gstDuplicateError.value = "GST already registered with another truck owner"
                } else if (response.isSuccessful) {
                    _gstDuplicateError.value = null
                }
            }
        }

        fun clearFocusErrors() {
            _panDuplicateError.value = null
            _gstDuplicateError.value = null
        }

        fun cancelEdit() {
            ifscLookupJob?.cancel()
            _isIfscLookupInProgress.value = false
            val temporaryKeys = _documentStates.value.values.mapNotNull { it.temporaryFileKey }
            val userId = originalProfile?.userId
            clearFocusErrors()
            _bankValidationErrors.value = emptyMap()
            _isEditing.value = false
            originalProfile?.let {
                populateEditableFields(it)
                initializeDocumentStates(it, preserveTemporary = false)
                populateBaseCity(it)
            }
            discardRouteChanges()
            recomputeHasChanges()
            if (userId != null && temporaryKeys.isNotEmpty()) {
                viewModelScope.launch(Dispatchers.IO) {
                    profileRepository.discardTemporaryDocuments(userId, temporaryKeys)
                }
            }
        }

        fun getUserInitials(): String {
            val name = originalProfile?.fullName ?: return "U"
            return name
                .split(" ")
                .filter { it.isNotEmpty() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
        }

        fun saveProfile() {
            val original = originalProfile ?: return
            if (_isSaving.value) return

            val bankErrors = validateBankChanges(original)
            if (bankErrors.isNotEmpty()) {
                _bankValidationErrors.value = bankErrors
                viewModelScope.launch {
                    _saveEvent.emit(Result.failure(IllegalStateException("Complete all required banking fields before saving")))
                }
                return
            }

            viewModelScope.launch(Dispatchers.IO) {
                _isSaving.value = true
                try {
                    val targetUserId = original.userId

                    val hasProfileChanges = hasProfileFieldChanges(original)
                    val hasBankChanges = hasBankChanges(original)
                    val hasRouteChanges = hasRouteChanges()
                    if (hasProfileChanges || hasRouteChanges) {
                        val request = buildSaveRequest(original)

                        val profileSaveResult = updateProfileUseCase.invoke(UpdateProfileParams(targetUserId, request))
                        if (profileSaveResult.isFailure) {
                            val message = profileSaveResult.exceptionOrNull()?.message ?: "Failed to save profile"
                            _saveEvent.emit(Result.failure(IllegalStateException(message)))
                            return@launch
                        }
                    }

                    val finalSaveRequest = buildFinalSaveRequest(targetUserId)
                    if (finalSaveRequest.files.isNotEmpty()) {
                        val finalSaveResult = finalizeIdentityDocumentsUseCase.invoke(finalSaveRequest)
                        if (finalSaveResult.isFailure) {
                            val message = finalSaveResult.exceptionOrNull()?.message ?: "Failed to finalize documents"
                            _saveEvent.emit(Result.failure(IllegalStateException(message)))
                            recomputeHasChanges()
                            return@launch
                        }
                        clearTemporaryFileKeys()
                    }

                    if (hasBankChanges) {
                        val chequeFile = _documentStates.value[
                            ProfileDocumentTypeIds.fieldKey(DocumentType.CANCELLED_CHEQUE)
                        ]?.pendingLocalFile
                        val bankSaveResult = profileRepository.saveBankAccount(
                            userId = targetUserId,
                            accountId = original.bankAccountDetails?.id,
                            accountHolderName = editableAccountHolderName.value.trim(),
                            accountNumber = editableAccountNumber.value.trim(),
                            ifscCode = editableIfscCode.value.trim(),
                            upiId = editableUpiId.value.trim(),
                            bankName = editableBankName.value.trim(),
                            branchName = editableBranchName.value.trim(),
                            chequeFile = chequeFile,
                        )
                        if (bankSaveResult.isFailure || bankSaveResult.getOrNull() != true) {
                            val message = bankSaveResult.exceptionOrNull()?.message ?: "Failed to save banking information"
                            _saveEvent.emit(Result.failure(IllegalStateException(message)))
                            recomputeHasChanges()
                            return@launch
                        }
                        clearPendingChequeFile()
                    }

                    _isEditing.value = false
                    fetchProfile(forceRefresh = true)
                    recomputeHasChanges()
                    _saveEvent.emit(Result.success(Unit))
                } catch (e: Exception) {
                    // _saveEvent is the single failure channel the screen collects;
                    // an earlier parallel _errorEvent flow was emitted here but never
                    // collected, so these messages were silently dropped.
                    _saveEvent.emit(Result.failure(e))
                } finally {
                    _isSaving.value = false
                }
            }
        }

        fun buildFinalSaveRequest(userId: String): FinalSaveRequest {
            return FinalSaveRequest(
                user_id = userId,
                files =
                    documentStates.value.values
                        .filter { it.temporaryFileKey != null }
                        .map { doc ->
                            FinalSaveFileRequest(
                                temporaryKey = doc.temporaryFileKey!!,
                                document_type_id = doc.documentTypeId,
                            )
                        },
            )
        }

        private fun buildSaveRequest(original: Profile): com.freight24.rest.request.UpdateProfileRequest {
            val baseCity = _baseCity.value
            return com.freight24.rest.request.UpdateProfileRequest(
                baseCityId = baseCity?.takeIf { it.id != original.baseCityId && it.type.equals("CITY", ignoreCase = true) }?.id,
                baseAreaId = baseCity?.takeIf { it.id != original.baseCityId && it.type.equals("AREA", ignoreCase = true) }?.id,
                full_name = changedValue(editableFullName.value, original.fullName),
                email = changedValue(editableEmailId.value, original.emailId),
                alt_phones =
                    changedValue(editableAlternativePhone.value, original.alternativePhoneNo.firstOrNull())
                        ?.let { value -> listOf(value).filter(String::isNotBlank) },
                pan_card = changedValue(editablePanNumber.value, original.panNumber),
                aadhar_card = changedValue(editableAadhaarNumber.value, original.aadhaarNumber),
                business_name = changedValue(editableBusinessName.value, original.businessName),
                gst_number = changedValue(editableGstNumber.value, original.gstNumber),
                preferredRoutes = gatherPreferredRouteEntries().takeIf { hasRouteChanges() },
            )
        }

        private fun changedValue(edited: String, stored: String?): String? {
            val normalizedEdited = edited.trim()
            return normalizedEdited.takeIf { it != stored.orEmpty().trim() }
        }

        fun onIfscChanged(value: String) {
            val code = value.uppercase().filter { it in 'A'..'Z' || it in '0'..'9' }.take(11)
            if (code == editableIfscCode.value) return
            ifscLookupJob?.cancel()
            _isIfscLookupInProgress.value = false
            editableIfscCode.value = code
            val originalBank = originalProfile?.bankAccountDetails
            if (code.equals(originalBank?.ifscCode, ignoreCase = true)) {
                editableBankName.value = originalBank?.bankName.orEmpty()
                editableBranchName.value = originalBank?.branchName.orEmpty()
            } else {
                editableBankName.value = ""
                editableBranchName.value = ""
            }
            recomputeHasChanges()
            refreshBankValidationErrors()
            ifscLookupCode(code)?.let(::lookupIfsc)
        }

        fun onBankFieldChanged() {
            recomputeHasChanges()
            refreshBankValidationErrors()
        }

        private fun lookupIfsc(ifsc: String) {
            val originalBank = originalProfile?.bankAccountDetails
            if (ifsc.equals(originalBank?.ifscCode, ignoreCase = true) &&
                editableBankName.value.isNotBlank() && editableBranchName.value.isNotBlank()
            ) return

            _isIfscLookupInProgress.value = true
            refreshBankValidationErrors()
            ifscLookupJob = viewModelScope.launch {
                var lookupError: String? = null
                val result = try {
                    withContext(Dispatchers.IO) { profileRepository.lookupIfsc(ifsc) }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    Result.failure(error)
                }
                coroutineContext.ensureActive()
                if (!_isEditing.value || editableIfscCode.value != ifsc) return@launch
                result
                    .onSuccess { bank ->
                        editableBankName.value = bank.bankName
                        editableBranchName.value = bank.branchName
                        recomputeHasChanges()
                    }
                    .onFailure { error ->
                        editableBankName.value = ""
                        editableBranchName.value = ""
                        lookupError = error.message ?: "Unable to verify IFSC code"
                    }
                _isIfscLookupInProgress.value = false
                refreshBankValidationErrors()
                if (lookupError != null) {
                    _bankValidationErrors.value = _bankValidationErrors.value + ("ifsc" to lookupError!!)
                }
            }
        }

        private fun refreshBankValidationErrors() {
            val original = originalProfile ?: return
            _bankValidationErrors.value = validateBankChanges(original)
        }

        private fun validateBankChanges(original: Profile): Map<String, String> {
            val bank = original.bankAccountDetails
            val hasBankChanges = hasBankChanges(original)
            if (!hasBankChanges) return emptyMap()

            val errors = mutableMapOf<String, String>()
            if (editableAccountHolderName.value.isBlank()) errors["accountHolder"] = "Account holder name is required"
            if (editableAccountNumber.value.isBlank()) errors["accountNumber"] = "Account number is required"
            if (editableIfscCode.value.isBlank()) errors["ifsc"] = "IFSC code is required"
            if (editableBankName.value.isBlank()) errors["bankName"] = "Validate IFSC to load the bank name"
            if (editableBranchName.value.isBlank()) errors["branchName"] = "Validate IFSC to load the branch name"
            val chequeState = _documentStates.value[ProfileDocumentTypeIds.fieldKey(DocumentType.CANCELLED_CHEQUE)]
            if (chequeState?.existingFileUrl.isNullOrBlank() && chequeState?.pendingLocalFile == null) {
                errors["cheque"] = "Cancelled cheque is required"
            }
            if (_isIfscLookupInProgress.value) errors["ifsc"] = "Wait for IFSC validation to finish"

            val ifscResult = ProfileFieldValidators.validateIfsc(editableIfscCode.value)
            if (editableIfscCode.value.isNotBlank() && ifscResult is ValidationResult.Error) {
                errors["ifsc"] = ifscResult.message
            }
            return errors
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

        fun uploadDocument(
            uris: List<Uri>,
            type: DocumentType,
            isPdf: Boolean,
        ) {
            if (uris.isEmpty()) return
            val original = originalProfile ?: return
            val fieldKey = ProfileDocumentTypeIds.fieldKey(type)
            val currentState = _documentStates.value[fieldKey] ?: return
            if (currentState.uploadStatus == UploadStatus.UPLOADING) return

            if (currentState.existingVerifyStatus == "verified" ||
                (type == DocumentType.CANCELLED_CHEQUE && original.bankAccountVerificationStatus)
            ) {
                viewModelScope.launch {
                    _uploadEvent.emit(Result.failure(IllegalStateException("Verified documents cannot be replaced")))
                }
                return
            }

            updateDocumentState(fieldKey) { state ->
                state.copy(uploadStatus = UploadStatus.UPLOADING, isPreparingDocument = true, existingVerifyStatus = null)
            }
            viewModelScope.launch(Dispatchers.IO) {
                recomputeHasChanges()

                try {
                    val file =
                        createPdfFile(uris = uris, isPdf = isPdf)
                            ?: throw IllegalStateException("Unable to process selected file")

                    DocumentUploadPolicy.validateFile(file)
                    updateDocumentState(fieldKey) { it.copy(isPreparingDocument = false) }
                    if (type == DocumentType.CANCELLED_CHEQUE) {
                        updateDocumentState(fieldKey) { state ->
                            state.copy(
                                pendingLocalFile = file,
                                existingVerifyStatus = null,
                                uploadStatus = UploadStatus.DONE,
                                isPreparingDocument = false,
                            )
                        }
                        _uploadEvent.emit(Result.success(Unit))
                        recomputeHasChanges()
                        return@launch
                    }
                    val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                    val result =
                        if (!currentState.existingDocId.isNullOrBlank()) {
                            updateIdentityDocumentUseCase.invoke(
                                UpdateIdentityDocumentParams(
                                    docId = currentState.existingDocId,
                                    userId = original.userId,
                                    documentNumber = "SESSION_$sessionId",
                                    file = body,
                                ),
                            )
                        } else {
                            uploadIdentityDocumentUseCase.invoke(
                                UploadIdentityDocumentParams(
                                    userId = original.userId,
                                    documentTypeId = currentState.documentTypeId,
                                    documentNumber = "SESSION_$sessionId",
                                    file = body,
                                ),
                            )
                        }

                    result.onSuccess { temporaryKey ->
                        val replacedTemporaryKey = currentState.temporaryFileKey
                        updateDocumentState(fieldKey) { state ->
                            state.copy(
                                temporaryFileKey = temporaryKey,
                                existingVerifyStatus = null,
                                uploadStatus = UploadStatus.DONE,
                                isPreparingDocument = false,
                            )
                        }
                        if (replacedTemporaryKey != null && replacedTemporaryKey != temporaryKey) {
                            profileRepository.discardTemporaryDocuments(original.userId, listOf(replacedTemporaryKey))
                        }
                        _uploadEvent.emit(Result.success(Unit))
                    }.onFailure { error ->
                        updateDocumentState(fieldKey) { state ->
                            state.copy(uploadStatus = UploadStatus.ERROR, isPreparingDocument = false)
                        }
                        _uploadEvent.emit(Result.failure(error))
                    }
                } catch (e: Exception) {
                    updateDocumentState(fieldKey) { state ->
                        state.copy(uploadStatus = UploadStatus.ERROR, isPreparingDocument = false)
                    }
                    _uploadEvent.emit(Result.failure(e))
                }

                recomputeHasChanges()
            }
        }

        private suspend fun createPdfFile(
            uris: List<Uri>,
            isPdf: Boolean,
        ): File? {
            return if (isPdf) {
                FileUtils.getFileFromUri(context, uris.first())
            } else {
                FileUtils.convertImageToPdf(context, uris)
            }
        }

        private fun updateDocumentState(
            fieldKey: String,
            transformer: (DocumentUploadState) -> DocumentUploadState,
        ) {
            _documentStates.value =
                _documentStates.value.toMutableMap().apply {
                    val current = this[fieldKey] ?: return@apply
                    this[fieldKey] = transformer(current)
                }
            recomputeDocumentDerivedState()
        }

        private fun clearTemporaryFileKeys() {
            _documentStates.value =
                _documentStates.value.mapValues { (_, state) ->
                    state.copy(temporaryFileKey = null, uploadStatus = UploadStatus.IDLE)
                }
            recomputeDocumentDerivedState()
        }

        private fun clearPendingChequeFile() {
            val fieldKey = ProfileDocumentTypeIds.fieldKey(DocumentType.CANCELLED_CHEQUE)
            updateDocumentState(fieldKey) { state ->
                state.copy(pendingLocalFile = null, uploadStatus = UploadStatus.IDLE)
            }
        }

        /**
         * Single writer for the two values derived from document state.
         * [recomputeHasChanges] also touches them; routing both through here
         * stops the two call sites drifting apart.
         */
        private fun recomputeDocumentDerivedState() {
            _hasPendingTemporaryUploads.value = hasTemporaryUploads()
            _completion.value =
                ProfileCompletion(
                    uploaded =
                        ProfileDocumentTypeIds.mandatoryIdentityDocumentTypes.count { type ->
                            val state = _documentStates.value[ProfileDocumentTypeIds.fieldKey(type)]
                            state != null &&
                                (!state.existingFileUrl.isNullOrBlank() || !state.temporaryFileKey.isNullOrBlank() || state.pendingLocalFile != null)
                        },
                    total = ProfileDocumentTypeIds.mandatoryIdentityDocumentTypes.size,
                )
        }

        private fun recomputeHasChanges() {
            val original = originalProfile
            val count = if (original == null) 0 else countChanges(original)
            recomputeDocumentDerivedState()
            _changeCount.value = count
            _hasChanges.value = count > 0
        }

        /**
         * Total number of pending edits, so the action bar can say *how many*
         * things Save will write rather than just enabling the button.
         */
        private fun countChanges(original: Profile): Int =
            countProfileFieldChanges(original) +
                countBankChanges(original) +
                stagedDocumentCount() +
                _routesToAdd.value.size +
                _routeIdsToDelete.value.size

        private fun countProfileFieldChanges(original: Profile): Int {
            val editedVsStored =
                listOf(
                    editableFullName.value to original.fullName.orEmpty(),
                    editableEmailId.value to original.emailId.orEmpty(),
                    editablePhoneNo.value to original.phoneNo.orEmpty(),
                    editableAadhaarNumber.value to original.aadhaarNumber.orEmpty(),
                    editablePanNumber.value to original.panNumber.orEmpty(),
                    editableAlternativePhone.value to original.alternativePhoneNo.firstOrNull().orEmpty(),
                    editableBusinessName.value to original.businessName.orEmpty(),
                    editableGstNumber.value to original.gstNumber.orEmpty(),
                )
            return editedVsStored.count { (edited, stored) -> edited != stored } +
                if (_baseCity.value?.id != original.baseCityId) 1 else 0
        }

        private fun hasProfileFieldChanges(original: Profile): Boolean = countProfileFieldChanges(original) > 0

        private fun countBankChanges(original: Profile): Int {
            val bank = original.bankAccountDetails
            return listOf(
                editableAccountHolderName.value.trim() to bank?.accountHolderName.orEmpty().trim(),
                editableAccountNumber.value.trim() to bank?.accountNumber.orEmpty().trim(),
                editableIfscCode.value.trim().uppercase() to bank?.ifscCode.orEmpty().trim().uppercase(),
                editableUpiId.value.trim() to bank?.upiId.orEmpty().trim(),
                editableBankName.value.trim() to bank?.bankName.orEmpty().trim(),
                editableBranchName.value.trim() to bank?.branchName.orEmpty().trim(),
            ).count { (edited, stored) -> edited != stored }
        }

        private fun hasBankChanges(original: Profile): Boolean =
            countBankChanges(original) > 0 ||
                _documentStates.value[ProfileDocumentTypeIds.fieldKey(DocumentType.CANCELLED_CHEQUE)]?.pendingLocalFile != null

        private fun stagedDocumentCount(): Int = _documentStates.value.values.count {
            !it.temporaryFileKey.isNullOrBlank() || it.pendingLocalFile != null
        }

        private fun hasTemporaryUploads(): Boolean {
            return _documentStates.value.values.any { !it.temporaryFileKey.isNullOrBlank() || it.pendingLocalFile != null }
        }

        private fun hasRouteChanges(): Boolean {
            return _routesToAdd.value.isNotEmpty() || _routeIdsToDelete.value.isNotEmpty()
        }

        fun getSignedUrl(
            documentId: String,
            onUrlReady: (String) -> Unit,
            onError: (Throwable) -> Unit,
        ) {
            if (!_previewingDocumentId.compareAndSet(null, documentId)) return
            viewModelScope.launch {
                try {
                    val signedUrl = withContext(Dispatchers.IO) {
                        getProfileSignedUrlUseCase.invoke(documentId).getOrThrow()
                    }
                    onUrlReady(signedUrl)
                } catch (error: CancellationException) {
                    onDocumentPreviewClosed()
                    throw error
                } catch (error: Exception) {
                    onDocumentPreviewClosed()
                    onError(error)
                }
            }
        }

        fun getCancelledChequeSignedUrl(
            bankAccountId: String,
            onUrlReady: (String) -> Unit,
            onError: (Throwable) -> Unit,
        ) {
            if (!_previewingDocumentId.compareAndSet(null, bankAccountId)) return
            viewModelScope.launch {
                try {
                    val signedUrl = withContext(Dispatchers.IO) {
                        profileRepository.getCancelledChequeSignedUrl(bankAccountId).getOrThrow()
                    }
                    onUrlReady(signedUrl)
                } catch (error: CancellationException) {
                    onDocumentPreviewClosed()
                    throw error
                } catch (error: Exception) {
                    onDocumentPreviewClosed()
                    onError(error)
                }
            }
        }

        fun onDocumentPreviewClosed() {
            _previewingDocumentId.value = null
        }

        fun logout() {
            viewModelScope.launch(Dispatchers.IO) {
                sessionManager.logout()
            }
        }

        /**
         * Refuses to reload while the user is editing.
         *
         * [fetchProfile] is not a pure state swap: it rewrites every editable
         * field, re-seeds `originalProfile` and calls [resetRouteStaging], which
         * discards staged route additions and deletions. A foreground refresh
         * landing mid-edit would therefore throw away unsaved work without any
         * warning, so it is skipped until the edit is saved or cancelled.
         */
        fun onRefresh() {
            if (_isEditing.value) return
            fetchProfile(forceRefresh = true)
        }

        fun retry() {
            fetchProfile()
        }

        private fun String.toPriorityInt(): Int =
            when (this) {
                "High" -> 1
                "Medium" -> 2
                else -> 3
            }
    }
