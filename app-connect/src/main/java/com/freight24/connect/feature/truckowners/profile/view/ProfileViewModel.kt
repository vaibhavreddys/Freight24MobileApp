package com.freight24.connect.feature.truckowners.profile.view

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.model.DocumentUploadState
import com.freight24.common.model.UploadMode
import com.freight24.common.model.UploadStatus
import com.freight24.common.util.FileUtils
import com.freight24.connect.feature.truckowners.profile.domain.DocumentType
import com.freight24.connect.feature.truckowners.profile.domain.PreferredRoute
import com.freight24.connect.feature.truckowners.profile.domain.Profile
import com.freight24.connect.feature.truckowners.profile.domain.ProfileRepository
import com.freight24.connect.feature.truckowners.profile.domain.usecase.FetchProfileParams
import com.freight24.connect.feature.truckowners.profile.domain.usecase.FetchProfileUseCase
import com.freight24.connect.feature.truckowners.profile.domain.usecase.FinalizeIdentityDocumentsUseCase
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UpdateIdentityDocumentParams
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UpdateIdentityDocumentUseCase
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UpdateProfileParams
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UpdateProfileUseCase
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UpdateTruckOwnerStatusParams
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UpdateTruckOwnerStatusUseCase
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UploadIdentityDocumentParams
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UploadIdentityDocumentUseCase
import com.freight24.connect.feature.truckowners.profile.util.ProfileDocumentTypeIds
import com.freight24.connect.repository.LocationRepository
import com.freight24.rest.api.TruckOwnerApi
import com.freight24.rest.request.FinalSaveFileRequest
import com.freight24.rest.request.FinalSaveRequest
import com.freight24.rest.request.PreferredRouteEntry
import com.freight24.rest.response.CityResult
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
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

@OptIn(FlowPreview::class)
@HiltViewModel
internal class ProfileViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val fetchProfileUseCase: FetchProfileUseCase,
        private val updateProfileUseCase: UpdateProfileUseCase,
        private val updateTruckOwnerStatusUseCase: UpdateTruckOwnerStatusUseCase,
        private val uploadIdentityDocumentUseCase: UploadIdentityDocumentUseCase,
        private val updateIdentityDocumentUseCase: UpdateIdentityDocumentUseCase,
        private val finalizeIdentityDocumentsUseCase: FinalizeIdentityDocumentsUseCase,
        private val getProfileSignedUrlUseCase: com.freight24.connect.feature.truckowners.profile.domain.usecase.GetProfileSignedUrlUseCase,
        private val profileRepository: ProfileRepository,
        private val locationRepository: LocationRepository,
        private val truckOwnerApi: TruckOwnerApi,
        @param:ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val ownerUserId: String = checkNotNull(savedStateHandle["ownerUserId"])
        val sessionId: String = UUID.randomUUID().toString()

        private val _uiState = MutableStateFlow<UiState<Profile>>(UiState.Loading)
        val uiState: StateFlow<UiState<Profile>> = _uiState.asStateFlow()

        private val _isEditing = MutableStateFlow(false)
        val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

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

        private val _documentStates = MutableStateFlow<Map<String, DocumentUploadState>>(emptyMap())
        val documentStates: StateFlow<Map<String, DocumentUploadState>> = _documentStates.asStateFlow()

        private val _hasChanges = MutableStateFlow(false)
        val hasChanges: StateFlow<Boolean> = _hasChanges.asStateFlow()

        private val _hasPendingTemporaryUploads = MutableStateFlow(false)
        val hasPendingTemporaryUploads: StateFlow<Boolean> = _hasPendingTemporaryUploads.asStateFlow()

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

        private val _isAccountActive = MutableStateFlow(true)
        val isAccountActive: StateFlow<Boolean> = _isAccountActive.asStateFlow()

        private val _isTogglingStatus = MutableStateFlow(false)
        val isTogglingStatus: StateFlow<Boolean> = _isTogglingStatus.asStateFlow()

        private val _panDuplicateError = MutableStateFlow<String?>(null)
        val panDuplicateError: StateFlow<String?> = _panDuplicateError.asStateFlow()

        private val _gstDuplicateError = MutableStateFlow<String?>(null)
        val gstDuplicateError: StateFlow<String?> = _gstDuplicateError.asStateFlow()

        private val _errorEvent = MutableSharedFlow<String>()
        val errorEvent = _errorEvent.asSharedFlow()

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

                fetchProfileUseCase.invoke(FetchProfileParams(ownerUserId))
                    .onSuccess { profile ->
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
                        val message = error.message ?: "Failed to fetch profile"
                        _uiState.value = UiState.Error(message)
                        _errorEvent.emit(message)
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
            _isAccountActive.value = profile.status?.lowercase() == "active"
        }

        private fun initializeDocumentStates(
            profile: Profile,
            preserveTemporary: Boolean,
        ) {
            val previous = _documentStates.value
            val docsByType = profile.documents.associateBy { it.type }

            _documentStates.value =
                ProfileDocumentTypeIds.editableDocumentTypes.associate { type ->
                    val fieldKey = ProfileDocumentTypeIds.fieldKey(type)
                    val existing = docsByType[type]
                    val oldState = previous[fieldKey]
                    val documentTypeId = ProfileDocumentTypeIds.documentTypeId(type) ?: 0

                    fieldKey to
                        DocumentUploadState(
                            fieldKey = fieldKey,
                            documentTypeId = documentTypeId,
                            existingDocId = existing?.docId,
                            existingFileUrl = existing?.url,
                            existingVerifyStatus = existing?.let { ProfileDocumentTypeIds.verifyStatus(it.status) },
                            temporaryFileKey = if (preserveTemporary) oldState?.temporaryFileKey else null,
                            uploadStatus =
                                if (preserveTemporary && oldState?.temporaryFileKey != null) {
                                    if (oldState.uploadStatus == UploadStatus.ERROR) UploadStatus.ERROR else UploadStatus.DONE
                                } else {
                                    UploadStatus.IDLE
                                },
                        )
                }

            _hasPendingTemporaryUploads.value = hasTemporaryUploads()
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
                baseCityQuery.value = baseLabel.orEmpty()
            } else {
                _baseCity.value = null
                baseCityQuery.value = baseLabel.orEmpty()
            }
            _baseCitySuggestions.value = emptyList()
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

        fun onBaseCityQueryChanged(query: String) {
            baseCityQuery.value = query
            if (query.isBlank()) {
                _baseCitySuggestions.value = emptyList()
                _baseLocation.value = null
            }
            if (_baseCity.value?.name != query) {
                _baseCity.value = null
            }
            recomputeHasChanges()
        }

        fun onBaseCitySelected(city: CityResult) {
            _baseCity.value = city
            baseCityQuery.value = city.name
            _baseCitySuggestions.value = emptyList()
            _baseLocation.value = city.name
            _baseCitySearchSkip.value = city.name
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
            viewModelScope.launch(Dispatchers.IO) {
                val response = truckOwnerApi.checkTruckOwnerField("pan_card", pan, excludeId = ownerUserId)
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
            viewModelScope.launch(Dispatchers.IO) {
                val response = truckOwnerApi.checkTruckOwnerField("gst_number", gst, excludeId = ownerUserId)
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
            clearFocusErrors()
            _isEditing.value = false
            originalProfile?.let {
                populateEditableFields(it)
                initializeDocumentStates(it, preserveTemporary = false)
                populateBaseCity(it)
            }
            discardRouteChanges()
            recomputeHasChanges()
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

            viewModelScope.launch(Dispatchers.IO) {
                _isSaving.value = true
                try {
                    val targetUserId = original.userId

                    val hasProfileChanges = hasProfileFieldChanges(original)
                    val hasRouteChanges = hasRouteChanges()
                    if (hasProfileChanges || hasRouteChanges) {
                        val request = buildSaveRequest(original)

                        val profileSaveResult = updateProfileUseCase.invoke(UpdateProfileParams(targetUserId, request))
                        if (profileSaveResult.isFailure) {
                            val message = profileSaveResult.exceptionOrNull()?.message ?: "Failed to save profile"
                            _errorEvent.emit(message)
                            _saveEvent.emit(Result.failure(IllegalStateException(message)))
                            return@launch
                        }
                    }

                    val finalSaveRequest = buildFinalSaveRequest(targetUserId)
                    if (finalSaveRequest.files.isNotEmpty()) {
                        val finalSaveResult = finalizeIdentityDocumentsUseCase.invoke(finalSaveRequest)
                        if (finalSaveResult.isFailure) {
                            val message = finalSaveResult.exceptionOrNull()?.message ?: "Failed to finalize documents"
                            _errorEvent.emit(message)
                            _saveEvent.emit(Result.failure(IllegalStateException(message)))
                            recomputeHasChanges()
                            return@launch
                        }
                        clearTemporaryFileKeys()
                    }

                    _isEditing.value = false
                    fetchProfile(forceRefresh = true)
                    recomputeHasChanges()
                    _saveEvent.emit(Result.success(Unit))
                } catch (e: Exception) {
                    val message = e.message ?: "Save failed"
                    _errorEvent.emit(message)
                    _saveEvent.emit(Result.failure(e))
                } finally {
                    _isSaving.value = false
                }
            }
        }

        fun buildFinalSaveRequest(targetUserId: String): FinalSaveRequest {
            return FinalSaveRequest(
                user_id = targetUserId,
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
                baseCityId = baseCity?.takeIf { it.type.equals("CITY", ignoreCase = true) }?.id,
                baseAreaId = baseCity?.takeIf { it.type.equals("AREA", ignoreCase = true) }?.id,
                full_name = editableFullName.value,
                email = editableEmailId.value,
                alt_phones = listOf(editableAlternativePhone.value),
                pan_card = editablePanNumber.value,
                aadhar_card = editableAadhaarNumber.value,
                business_name = editableBusinessName.value,
                gst_number = editableGstNumber.value,
                is_tds_applicable = original.isTdsApplicable,
                account_holder_name = editableAccountHolderName.value,
                account_number = editableAccountNumber.value,
                ifsc_code = editableIfscCode.value,
                upi_id = editableUpiId.value,
                bank_name = editableBankName.value,
                branch_name = editableBranchName.value,
                preferredRoutes = gatherPreferredRouteEntries(),
            )
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

            viewModelScope.launch(Dispatchers.IO) {
                updateDocumentState(fieldKey) { state ->
                    state.copy(uploadStatus = UploadStatus.UPLOADING, existingVerifyStatus = null)
                }
                recomputeHasChanges()

                try {
                    val file =
                        createPdfFile(uris = uris, isPdf = isPdf)
                            ?: throw IllegalStateException("Unable to process selected file")

                    val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                    val result =
                        when (val uploadMode = resolveUploadMode(currentState)) {
                            UploadMode.ADD -> {
                                uploadIdentityDocumentUseCase.invoke(
                                    UploadIdentityDocumentParams(
                                        userId = original.userId,
                                        documentTypeId = currentState.documentTypeId,
                                        documentNumber = "SESSION_$sessionId",
                                        file = body,
                                    ),
                                )
                            }

                            is UploadMode.UPDATE -> {
                                updateIdentityDocumentUseCase.invoke(
                                    UpdateIdentityDocumentParams(
                                        docId = uploadMode.existingDocId,
                                        userId = original.userId,
                                        documentNumber = "SESSION_$sessionId",
                                        file = body,
                                    ),
                                )
                            }
                        }

                    result.onSuccess { temporaryKey ->
                        updateDocumentState(fieldKey) { state ->
                            state.copy(
                                temporaryFileKey = temporaryKey,
                                existingVerifyStatus = null,
                                uploadStatus = UploadStatus.DONE,
                            )
                        }
                    }.onFailure { error ->
                        updateDocumentState(fieldKey) { state ->
                            state.copy(uploadStatus = UploadStatus.ERROR)
                        }
                        _errorEvent.emit(error.message ?: "Upload failed")
                    }
                } catch (e: Exception) {
                    updateDocumentState(fieldKey) { state ->
                        state.copy(uploadStatus = UploadStatus.ERROR)
                    }
                    _errorEvent.emit(e.message ?: "An error occurred during upload")
                }

                recomputeHasChanges()
            }
        }

        private fun resolveUploadMode(currentDoc: DocumentUploadState): UploadMode {
            val existingDocId = currentDoc.existingDocId?.takeIf { it.isNotBlank() }
            return if (existingDocId == null) {
                UploadMode.ADD
            } else {
                UploadMode.UPDATE(existingDocId = existingDocId)
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
            _hasPendingTemporaryUploads.value = hasTemporaryUploads()
        }

        private fun clearTemporaryFileKeys() {
            _documentStates.value =
                _documentStates.value.mapValues { (_, state) ->
                    state.copy(temporaryFileKey = null, uploadStatus = UploadStatus.IDLE)
                }
            _hasPendingTemporaryUploads.value = false
        }

        private fun recomputeHasChanges() {
            val original = originalProfile
            val textChanged = if (original == null) false else hasProfileFieldChanges(original)

            val hasTemporaryUploads = hasTemporaryUploads()
            val routeChanges = hasRouteChanges()
            _hasPendingTemporaryUploads.value = hasTemporaryUploads
            _hasChanges.value = textChanged || hasTemporaryUploads || routeChanges
        }

        private fun hasProfileFieldChanges(original: Profile): Boolean {
            return editableFullName.value != original.fullName.orEmpty() ||
                editableEmailId.value != original.emailId.orEmpty() ||
                editablePhoneNo.value != original.phoneNo.orEmpty() ||
                editableAadhaarNumber.value != original.aadhaarNumber.orEmpty() ||
                editablePanNumber.value != original.panNumber.orEmpty() ||
                editableAlternativePhone.value != original.alternativePhoneNo.firstOrNull().orEmpty() ||
                editableBusinessName.value != original.businessName.orEmpty() ||
                editableGstNumber.value != original.gstNumber.orEmpty() ||
                editableAccountHolderName.value != original.bankAccountDetails?.accountHolderName.orEmpty() ||
                editableAccountNumber.value != original.bankAccountDetails?.accountNumber.orEmpty() ||
                editableIfscCode.value != original.bankAccountDetails?.ifscCode.orEmpty() ||
                editableUpiId.value != original.bankAccountDetails?.upiId.orEmpty() ||
                editableBankName.value != original.bankAccountDetails?.bankName.orEmpty() ||
                editableBranchName.value != original.bankAccountDetails?.branchName.orEmpty() ||
                _baseCity.value?.id != original.baseCityId
        }

        private fun hasTemporaryUploads(): Boolean {
            return _documentStates.value.values.any { !it.temporaryFileKey.isNullOrBlank() }
        }

        private fun hasRouteChanges(): Boolean {
            return _routesToAdd.value.isNotEmpty() || _routeIdsToDelete.value.isNotEmpty()
        }

        fun getSignedUrl(
            documentId: String,
            onUrlReady: (String) -> Unit,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                getProfileSignedUrlUseCase.invoke(documentId)
                    .onSuccess { signedUrl ->
                        onUrlReady(signedUrl)
                    }
                    .onFailure { error ->
                        _errorEvent.emit(error.message ?: "Failed to get document link")
                    }
            }
        }

        fun onRefresh() {
            fetchProfile(forceRefresh = true)
        }

        fun retry() {
            fetchProfile()
        }

        fun toggleAccountStatus() {
            val original = originalProfile ?: return
            if (_isTogglingStatus.value) return

            viewModelScope.launch(Dispatchers.IO) {
                _isTogglingStatus.value = true
                val newStatus = if (_isAccountActive.value) "inactive" else "active"

                updateTruckOwnerStatusUseCase.invoke(
                    UpdateTruckOwnerStatusParams(original.userId, newStatus),
                ).onSuccess {
                    _isAccountActive.value = newStatus == "active"
                    val current = _uiState.value
                    if (current is UiState.Success) {
                        _uiState.value = UiState.Success(current.data.copy(status = newStatus))
                    }
                }.onFailure { error ->
                    _errorEvent.emit(error.message ?: "Failed to update account status")
                }

                _isTogglingStatus.value = false
            }
        }

        private fun String.toPriorityInt(): Int =
            when (this) {
                "High" -> 1
                "Medium" -> 2
                else -> 3
            }

        private fun Throwable?.isRouteNotFound(): Boolean {
            val legacyHttp = this as? com.freight24.network.error.NetworkException.HttpException
            val apiError = this as? com.freight24.network.error.ApiException
            val statusCode = apiError?.statusCode ?: legacyHttp?.code ?: return false
            val errorText = apiError?.rawBody ?: legacyHttp?.errorBody
            return statusCode == 404 && errorText?.contains("Route not found", ignoreCase = true) == true
        }
    }
