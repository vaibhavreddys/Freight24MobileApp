package com.freight24.connect.feature.verificationrequests.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.verificationrequests.domain.model.ClientVerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.TruckOwnerVerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.TruckVerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestDocument
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestStatus
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestTab
import com.freight24.connect.feature.verificationrequests.domain.usecase.GetVerificationClientRequestsUseCase
import com.freight24.connect.feature.verificationrequests.domain.usecase.GetVerificationRequestsUseCase
import com.freight24.connect.feature.verificationrequests.domain.usecase.GetVerificationSignedUrlUseCase
import com.freight24.connect.feature.verificationrequests.domain.usecase.GetVerificationTruckOwnerRequestsUseCase
import com.freight24.connect.feature.verificationrequests.domain.usecase.GetVerificationTruckRequestsUseCase
import com.freight24.connect.feature.verificationrequests.domain.usecase.UpdateClientVerificationStatusUseCase
import com.freight24.connect.feature.verificationrequests.domain.usecase.UpdateClientVerificationStatusUseCaseParams
import com.freight24.connect.feature.verificationrequests.domain.usecase.UpdateTruckOwnerVerificationStatusUseCase
import com.freight24.connect.feature.verificationrequests.domain.usecase.UpdateTruckOwnerVerificationStatusUseCaseParams
import com.freight24.connect.feature.verificationrequests.domain.usecase.UpdateTruckVerificationStatusUseCase
import com.freight24.connect.feature.verificationrequests.domain.usecase.UpdateTruckVerificationStatusUseCaseParams
import com.freight24.connect.feature.verificationrequests.domain.usecase.UpdateVerificationRequestStatusUseCase
import com.freight24.connect.feature.verificationrequests.domain.usecase.UpdateVerificationRequestStatusUseCaseParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class VerificationRequestsViewModelImpl
    @Inject
    constructor(
        private val getVerificationRequestsUseCase: GetVerificationRequestsUseCase,
        private val getVerificationTruckRequestsUseCase: GetVerificationTruckRequestsUseCase,
        private val getVerificationTruckOwnerRequestsUseCase: GetVerificationTruckOwnerRequestsUseCase,
        private val getVerificationClientRequestsUseCase: GetVerificationClientRequestsUseCase,
        private val getVerificationSignedUrlUseCase: GetVerificationSignedUrlUseCase,
        private val updateVerificationRequestStatusUseCase: UpdateVerificationRequestStatusUseCase,
        private val updateTruckVerificationStatusUseCase: UpdateTruckVerificationStatusUseCase,
        private val updateTruckOwnerVerificationStatusUseCase: UpdateTruckOwnerVerificationStatusUseCase,
        private val updateClientVerificationStatusUseCase: UpdateClientVerificationStatusUseCase,
    ) : ViewModel(), VerificationRequestsViewModel {
        private val _uiState = MutableStateFlow(VerificationRequestsUiState())
        override val uiState: StateFlow<VerificationRequestsUiState> = _uiState.asStateFlow()

        init {
            fetchTrips()
        }

        override fun onEvent(event: VerificationRequestsEvent) {
            when (event) {
                is VerificationRequestsEvent.SelectTab -> {
                    _uiState.update { it.copy(selectedTab = event.tab, errorMessage = null) }
                    when (event.tab) {
                        VerificationRequestTab.TRIPS -> ensureTripsLoaded()
                        VerificationRequestTab.TRUCKS -> ensureTrucksLoaded()
                        VerificationRequestTab.TRUCK_OWNERS -> ensureTruckOwnersLoaded()
                        VerificationRequestTab.CLIENTS -> ensureClientsLoaded()
                    }
                }

                is VerificationRequestsEvent.SelectStatus -> {
                    val previousStatus = _uiState.value.selectedStatus
                    _uiState.update { it.copy(selectedStatus = event.status, errorMessage = null) }
                    when (_uiState.value.selectedTab) {
                        VerificationRequestTab.TRIPS -> handleStatusChangeForTrips(previousStatus, event.status)
                        VerificationRequestTab.TRUCKS -> handleStatusChangeForTrucks(previousStatus, event.status)
                        VerificationRequestTab.TRUCK_OWNERS -> handleStatusChangeForTruckOwners(previousStatus, event.status)
                        VerificationRequestTab.CLIENTS -> handleStatusChangeForClients(previousStatus, event.status)
                    }
                }

                VerificationRequestsEvent.Refresh -> refreshCurrentSelection()

                VerificationRequestsEvent.ClearActionState -> {
                    _uiState.update {
                        it.copy(
                            actionErrorMessage = null,
                            actionInProgressRequestId = null,
                            actionInProgressStatus = null,
                        )
                    }
                }

                is VerificationRequestsEvent.UpdateTripStatus ->
                    updateTripStatus(
                        request = event.request,
                        status = event.status,
                        resolutionNotes = event.resolutionNotes,
                    )

                is VerificationRequestsEvent.UpdateTruckStatus ->
                    updateTruckStatus(
                        request = event.request,
                        status = event.status,
                        resolutionNotes = event.resolutionNotes,
                    )

                is VerificationRequestsEvent.UpdateTruckOwnerStatus ->
                    updateTruckOwnerStatus(
                        request = event.request,
                        status = event.status,
                        resolutionNotes = event.resolutionNotes,
                    )

                is VerificationRequestsEvent.UpdateClientStatus ->
                    updateClientStatus(
                        request = event.request,
                        status = event.status,
                        resolutionNotes = event.resolutionNotes,
                    )
            }
        }

        override fun getSignedUrl(
            document: VerificationRequestDocument,
            onUrlReady: (String) -> Unit,
        ) {
            viewModelScope.launch {
                getVerificationSignedUrlUseCase(document.entity, document.requestId, document.name)
                    .onSuccess(onUrlReady)
            }
        }

        private fun handleStatusChangeForTrips(
            previousStatus: VerificationRequestStatus,
            newStatus: VerificationRequestStatus,
        ) {
            if (newStatus == VerificationRequestStatus.COMPLETED && !_uiState.value.loadedCompletedTrips) {
                fetchCompletedTrips()
            } else if (
                previousStatus == VerificationRequestStatus.COMPLETED &&
                newStatus != VerificationRequestStatus.COMPLETED &&
                !_uiState.value.loadedDefaultTrips
            ) {
                fetchTrips()
            }
        }

        private fun handleStatusChangeForTrucks(
            previousStatus: VerificationRequestStatus,
            newStatus: VerificationRequestStatus,
        ) {
            if (newStatus == VerificationRequestStatus.COMPLETED && !_uiState.value.loadedCompletedTrucks) {
                fetchCompletedTrucks()
            } else if (
                previousStatus == VerificationRequestStatus.COMPLETED &&
                newStatus != VerificationRequestStatus.COMPLETED &&
                !_uiState.value.loadedDefaultTrucks
            ) {
                fetchTrucks()
            }
        }

        private fun handleStatusChangeForTruckOwners(
            previousStatus: VerificationRequestStatus,
            newStatus: VerificationRequestStatus,
        ) {
            if (newStatus == VerificationRequestStatus.COMPLETED && !_uiState.value.loadedCompletedTruckOwners) {
                fetchCompletedTruckOwners()
            } else if (
                previousStatus == VerificationRequestStatus.COMPLETED &&
                newStatus != VerificationRequestStatus.COMPLETED &&
                !_uiState.value.loadedDefaultTruckOwners
            ) {
                fetchTruckOwners()
            }
        }

        private fun handleStatusChangeForClients(
            previousStatus: VerificationRequestStatus,
            newStatus: VerificationRequestStatus,
        ) {
            if (newStatus == VerificationRequestStatus.COMPLETED && !_uiState.value.loadedCompletedClients) {
                fetchCompletedClients()
            } else if (
                previousStatus == VerificationRequestStatus.COMPLETED &&
                newStatus != VerificationRequestStatus.COMPLETED &&
                !_uiState.value.loadedDefaultClients
            ) {
                fetchClients()
            }
        }

        private fun refreshCurrentSelection() {
            when (_uiState.value.selectedTab) {
                VerificationRequestTab.TRIPS -> refreshTrips()
                VerificationRequestTab.TRUCKS -> refreshTrucks()
                VerificationRequestTab.TRUCK_OWNERS -> refreshTruckOwners()
                VerificationRequestTab.CLIENTS -> refreshClients()
            }
        }

        private fun refreshTrips() {
            when (_uiState.value.selectedStatus) {
                VerificationRequestStatus.COMPLETED -> fetchCompletedTrips(forceRefresh = true, isRefreshing = true)
                VerificationRequestStatus.PENDING,
                VerificationRequestStatus.IN_REVIEW,
                -> fetchTrips(forceRefresh = true, isRefreshing = true)
                VerificationRequestStatus.APPROVED,
                VerificationRequestStatus.REJECTED,
                VerificationRequestStatus.VERIFIED,
                -> fetchCompletedTrips(forceRefresh = true, isRefreshing = true)
            }
        }

        private fun refreshTrucks() {
            when (_uiState.value.selectedStatus) {
                VerificationRequestStatus.COMPLETED -> fetchCompletedTrucks(forceRefresh = true, isRefreshing = true)
                VerificationRequestStatus.PENDING,
                VerificationRequestStatus.IN_REVIEW,
                -> fetchTrucks(forceRefresh = true, isRefreshing = true)
                else -> Unit
            }
        }

        private fun refreshTruckOwners() {
            when (_uiState.value.selectedStatus) {
                VerificationRequestStatus.COMPLETED -> fetchCompletedTruckOwners(forceRefresh = true, isRefreshing = true)
                VerificationRequestStatus.PENDING,
                VerificationRequestStatus.IN_REVIEW,
                -> fetchTruckOwners(forceRefresh = true, isRefreshing = true)
                else -> Unit
            }
        }

        private fun refreshClients() {
            when (_uiState.value.selectedStatus) {
                VerificationRequestStatus.COMPLETED -> fetchCompletedClients(forceRefresh = true, isRefreshing = true)
                VerificationRequestStatus.PENDING,
                VerificationRequestStatus.IN_REVIEW,
                -> fetchClients(forceRefresh = true, isRefreshing = true)
                else -> Unit
            }
        }

        private fun ensureTripsLoaded() {
            when (_uiState.value.selectedStatus) {
                VerificationRequestStatus.COMPLETED ->
                    if (!_uiState.value.loadedCompletedTrips) {
                        fetchCompletedTrips()
                    }

                VerificationRequestStatus.PENDING,
                VerificationRequestStatus.IN_REVIEW,
                ->
                    if (!_uiState.value.loadedDefaultTrips) {
                        fetchTrips()
                    }
                VerificationRequestStatus.APPROVED,
                VerificationRequestStatus.REJECTED,
                VerificationRequestStatus.VERIFIED,
                ->
                    if (!_uiState.value.loadedCompletedTrips) {
                        fetchCompletedTrips()
                    }
            }
        }

        private fun ensureTrucksLoaded() {
            when (_uiState.value.selectedStatus) {
                VerificationRequestStatus.COMPLETED ->
                    if (!_uiState.value.loadedCompletedTrucks) {
                        fetchCompletedTrucks()
                    }

                VerificationRequestStatus.PENDING,
                VerificationRequestStatus.IN_REVIEW,
                ->
                    if (!_uiState.value.loadedDefaultTrucks) {
                        fetchTrucks()
                    }
                else -> Unit
            }
        }

        private fun ensureTruckOwnersLoaded() {
            when (_uiState.value.selectedStatus) {
                VerificationRequestStatus.COMPLETED ->
                    if (!_uiState.value.loadedCompletedTruckOwners) {
                        fetchCompletedTruckOwners()
                    }

                VerificationRequestStatus.PENDING,
                VerificationRequestStatus.IN_REVIEW,
                ->
                    if (!_uiState.value.loadedDefaultTruckOwners) {
                        fetchTruckOwners()
                    }
                else -> Unit
            }
        }

        private fun ensureClientsLoaded() {
            when (_uiState.value.selectedStatus) {
                VerificationRequestStatus.COMPLETED ->
                    if (!_uiState.value.loadedCompletedClients) {
                        fetchCompletedClients()
                    }

                VerificationRequestStatus.PENDING,
                VerificationRequestStatus.IN_REVIEW,
                ->
                    if (!_uiState.value.loadedDefaultClients) {
                        fetchClients()
                    }
                else -> Unit
            }
        }

        private fun fetchTrips(
            forceRefresh: Boolean = false,
            isRefreshing: Boolean = false,
        ) {
            if (_uiState.value.loadingStatus == VerificationRequestStatus.PENDING || _uiState.value.loadingStatus == VerificationRequestStatus.IN_REVIEW) return
            if (_uiState.value.actionInProgressRequestId != null) return

            viewModelScope.launch {
                val currentStatus = _uiState.value.selectedStatus
                _uiState.update {
                    it.copy(
                        isLoading = !isRefreshing,
                        isRefreshing = isRefreshing,
                        loadingStatus = currentStatus,
                        errorMessage = null,
                        actionErrorMessage = null,
                    )
                }
                getVerificationRequestsUseCase(null)
                    .onSuccess { requests ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                loadingStatus = null,
                                defaultTrips = requests,
                                loadedDefaultTrips = true,
                                errorMessage = null,
                                actionErrorMessage = null,
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                loadingStatus = null,
                                errorMessage = error.message ?: "Failed to load verification requests",
                            )
                        }
                    }
            }
        }

        private fun fetchCompletedTrips(
            forceRefresh: Boolean = false,
            isRefreshing: Boolean = false,
        ) {
            if (_uiState.value.loadingStatus == VerificationRequestStatus.COMPLETED && !forceRefresh) return
            if (_uiState.value.actionInProgressRequestId != null) return

            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isLoading = !isRefreshing,
                        isRefreshing = isRefreshing,
                        loadingStatus = VerificationRequestStatus.COMPLETED,
                        errorMessage = null,
                        actionErrorMessage = null,
                    )
                }
                getVerificationRequestsUseCase(VerificationRequestStatus.COMPLETED)
                    .onSuccess { requests ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                loadingStatus = null,
                                completedTrips = requests,
                                loadedCompletedTrips = true,
                                errorMessage = null,
                                actionErrorMessage = null,
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                loadingStatus = null,
                                errorMessage = error.message ?: "Failed to load verification requests",
                            )
                        }
                    }
            }
        }

        private fun fetchTrucks(
            forceRefresh: Boolean = false,
            isRefreshing: Boolean = false,
        ) {
            if (_uiState.value.loadingStatus == VerificationRequestStatus.PENDING || _uiState.value.loadingStatus == VerificationRequestStatus.IN_REVIEW) return
            if (_uiState.value.actionInProgressRequestId != null) return

            viewModelScope.launch {
                val currentStatus = _uiState.value.selectedStatus
                _uiState.update {
                    it.copy(
                        isLoading = !isRefreshing,
                        isRefreshing = isRefreshing,
                        loadingStatus = currentStatus,
                        errorMessage = null,
                        actionErrorMessage = null,
                    )
                }
                getVerificationTruckRequestsUseCase(null)
                    .onSuccess { requests ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                loadingStatus = null,
                                defaultTrucks = requests,
                                loadedDefaultTrucks = true,
                                errorMessage = null,
                                actionErrorMessage = null,
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                loadingStatus = null,
                                errorMessage = error.message ?: "Failed to load truck verification requests",
                            )
                        }
                    }
            }
        }

        private fun fetchCompletedTrucks(
            forceRefresh: Boolean = false,
            isRefreshing: Boolean = false,
        ) {
            if (_uiState.value.loadingStatus == VerificationRequestStatus.COMPLETED && !forceRefresh) return
            if (_uiState.value.actionInProgressRequestId != null) return

            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isLoading = !isRefreshing,
                        isRefreshing = isRefreshing,
                        loadingStatus = VerificationRequestStatus.COMPLETED,
                        errorMessage = null,
                        actionErrorMessage = null,
                    )
                }
                getVerificationTruckRequestsUseCase(VerificationRequestStatus.COMPLETED)
                    .onSuccess { requests ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                loadingStatus = null,
                                completedTrucks = requests,
                                loadedCompletedTrucks = true,
                                errorMessage = null,
                                actionErrorMessage = null,
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                loadingStatus = null,
                                errorMessage = error.message ?: "Failed to load truck verification requests",
                            )
                        }
                    }
            }
        }

        private fun fetchTruckOwners(
            forceRefresh: Boolean = false,
            isRefreshing: Boolean = false,
        ) {
            if ((_uiState.value.loadingStatus == VerificationRequestStatus.PENDING || _uiState.value.loadingStatus == VerificationRequestStatus.IN_REVIEW) && !forceRefresh) return
            if (_uiState.value.actionInProgressRequestId != null) return

            viewModelScope.launch {
                val currentStatus = _uiState.value.selectedStatus
                _uiState.update {
                    it.copy(
                        isLoading = !isRefreshing,
                        isRefreshing = isRefreshing,
                        loadingStatus = currentStatus,
                        errorMessage = null,
                        actionErrorMessage = null,
                    )
                }
                getVerificationTruckOwnerRequestsUseCase(null)
                    .onSuccess { requests ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                loadingStatus = null,
                                defaultTruckOwners = requests,
                                loadedDefaultTruckOwners = true,
                                errorMessage = null,
                                actionErrorMessage = null,
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                loadingStatus = null,
                                errorMessage = error.message ?: "Failed to load truck owner verification requests",
                            )
                        }
                    }
            }
        }

        private fun fetchCompletedTruckOwners(
            forceRefresh: Boolean = false,
            isRefreshing: Boolean = false,
        ) {
            if (_uiState.value.loadingStatus == VerificationRequestStatus.COMPLETED && !forceRefresh) return
            if (_uiState.value.actionInProgressRequestId != null) return

            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isLoading = !isRefreshing,
                        isRefreshing = isRefreshing,
                        loadingStatus = VerificationRequestStatus.COMPLETED,
                        errorMessage = null,
                        actionErrorMessage = null,
                    )
                }
                getVerificationTruckOwnerRequestsUseCase(VerificationRequestStatus.COMPLETED)
                    .onSuccess { requests ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                loadingStatus = null,
                                completedTruckOwners = requests,
                                loadedCompletedTruckOwners = true,
                                errorMessage = null,
                                actionErrorMessage = null,
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                loadingStatus = null,
                                errorMessage = error.message ?: "Failed to load truck owner verification requests",
                            )
                        }
                    }
            }
        }

        private fun fetchClients(
            forceRefresh: Boolean = false,
            isRefreshing: Boolean = false,
        ) {
            if ((_uiState.value.loadingStatus == VerificationRequestStatus.PENDING || _uiState.value.loadingStatus == VerificationRequestStatus.IN_REVIEW) && !forceRefresh) return
            if (_uiState.value.actionInProgressRequestId != null) return

            viewModelScope.launch {
                val currentStatus = _uiState.value.selectedStatus
                _uiState.update {
                    it.copy(
                        isLoading = !isRefreshing,
                        isRefreshing = isRefreshing,
                        loadingStatus = currentStatus,
                        errorMessage = null,
                        actionErrorMessage = null,
                    )
                }
                getVerificationClientRequestsUseCase(null)
                    .onSuccess { requests ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                loadingStatus = null,
                                defaultClients = requests,
                                loadedDefaultClients = true,
                                errorMessage = null,
                                actionErrorMessage = null,
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                loadingStatus = null,
                                errorMessage = error.message ?: "Failed to load client verification requests",
                            )
                        }
                    }
            }
        }

        private fun fetchCompletedClients(
            forceRefresh: Boolean = false,
            isRefreshing: Boolean = false,
        ) {
            if (_uiState.value.loadingStatus == VerificationRequestStatus.COMPLETED && !forceRefresh) return
            if (_uiState.value.actionInProgressRequestId != null) return

            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isLoading = !isRefreshing,
                        isRefreshing = isRefreshing,
                        loadingStatus = VerificationRequestStatus.COMPLETED,
                        errorMessage = null,
                        actionErrorMessage = null,
                    )
                }
                getVerificationClientRequestsUseCase(VerificationRequestStatus.COMPLETED)
                    .onSuccess { requests ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                loadingStatus = null,
                                completedClients = requests,
                                loadedCompletedClients = true,
                                errorMessage = null,
                                actionErrorMessage = null,
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                loadingStatus = null,
                                errorMessage = error.message ?: "Failed to load client verification requests",
                            )
                        }
                    }
            }
        }

        private fun updateTripStatus(
            request: VerificationRequest,
            status: VerificationRequestStatus,
            resolutionNotes: String?,
        ) {
            if (_uiState.value.actionInProgressRequestId != null) return

            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        actionInProgressRequestId = request.id,
                        actionInProgressStatus = status,
                        actionErrorMessage = null,
                        errorMessage = null,
                    )
                }

                updateVerificationRequestStatusUseCase(
                    UpdateVerificationRequestStatusUseCaseParams(
                        id = request.id,
                        status = status,
                        resolutionNotes = resolutionNotes,
                    ),
                ).fold(
                    onSuccess = {
                        val updatedRequest =
                            request.copy(
                                status = status,
                                resolutionNotes = resolutionNotes?.takeUnless { it.isBlank() },
                            )
                        _uiState.update { state ->
                            state.copy(
                                defaultTrips = state.defaultTrips.filterNot { it.id == request.id },
                                completedTrips =
                                    state.completedTrips
                                        .filterNot { it.id == request.id }
                                        .let { existing ->
                                            listOf(updatedRequest) + existing
                                        },
                                actionInProgressRequestId = null,
                                actionInProgressStatus = null,
                                actionCompletedAt = System.currentTimeMillis(),
                                actionErrorMessage = null,
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                actionInProgressRequestId = null,
                                actionInProgressStatus = null,
                                actionErrorMessage = error.message ?: "Failed to update verification request",
                            )
                        }
                    },
                )
            }
        }

        private fun updateTruckStatus(
            request: TruckVerificationRequest,
            status: VerificationRequestStatus,
            resolutionNotes: String?,
        ) {
            if (_uiState.value.actionInProgressRequestId != null) return

            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        actionInProgressRequestId = request.id,
                        actionInProgressStatus = status,
                        actionErrorMessage = null,
                        errorMessage = null,
                    )
                }

                updateTruckVerificationStatusUseCase(
                    UpdateTruckVerificationStatusUseCaseParams(
                        id = request.id,
                        status = status.apiValue,
                        resolutionNotes = resolutionNotes,
                    ),
                ).fold(
                    onSuccess = {
                        val updatedRequest =
                            request.copy(
                                status = status,
                                resolutionNotes = resolutionNotes?.takeUnless { it.isBlank() },
                            )
                        _uiState.update { state ->
                            state.copy(
                                defaultTrucks = state.defaultTrucks.filterNot { it.id == request.id },
                                completedTrucks =
                                    state.completedTrucks
                                        .filterNot { it.id == request.id }
                                        .let { existing ->
                                            listOf(updatedRequest) + existing
                                        },
                                actionInProgressRequestId = null,
                                actionInProgressStatus = null,
                                actionCompletedAt = System.currentTimeMillis(),
                                actionErrorMessage = null,
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                actionInProgressRequestId = null,
                                actionInProgressStatus = null,
                                actionErrorMessage = error.message ?: "Failed to update truck verification request",
                            )
                        }
                    },
                )
            }
        }

        private fun updateTruckOwnerStatus(
            request: TruckOwnerVerificationRequest,
            status: VerificationRequestStatus,
            resolutionNotes: String?,
        ) {
            if (_uiState.value.actionInProgressRequestId != null) return

            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        actionInProgressRequestId = request.id,
                        actionInProgressStatus = status,
                        actionErrorMessage = null,
                        errorMessage = null,
                    )
                }

                updateTruckOwnerVerificationStatusUseCase(
                    UpdateTruckOwnerVerificationStatusUseCaseParams(
                        id = request.id,
                        status = status.apiValue,
                        resolutionNotes = resolutionNotes,
                    ),
                ).fold(
                    onSuccess = {
                        val updatedRequest =
                            request.copy(
                                status = status,
                                resolutionNotes = resolutionNotes?.takeUnless { it.isBlank() },
                            )
                        _uiState.update { state ->
                            state.copy(
                                defaultTruckOwners = state.defaultTruckOwners.filterNot { it.id == request.id },
                                completedTruckOwners =
                                    state.completedTruckOwners
                                        .filterNot { it.id == request.id }
                                        .let { existing -> listOf(updatedRequest) + existing },
                                actionInProgressRequestId = null,
                                actionInProgressStatus = null,
                                actionCompletedAt = System.currentTimeMillis(),
                                actionErrorMessage = null,
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                actionInProgressRequestId = null,
                                actionInProgressStatus = null,
                                actionErrorMessage = error.message ?: "Failed to update truck owner verification request",
                            )
                        }
                    },
                )
            }
        }

        private fun updateClientStatus(
            request: ClientVerificationRequest,
            status: VerificationRequestStatus,
            resolutionNotes: String?,
        ) {
            if (_uiState.value.actionInProgressRequestId != null) return

            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        actionInProgressRequestId = request.id,
                        actionInProgressStatus = status,
                        actionErrorMessage = null,
                        errorMessage = null,
                    )
                }

                updateClientVerificationStatusUseCase(
                    UpdateClientVerificationStatusUseCaseParams(
                        id = request.id,
                        status = status.apiValue,
                        resolutionNotes = resolutionNotes,
                    ),
                ).fold(
                    onSuccess = {
                        val updatedRequest =
                            request.copy(
                                status = status,
                                resolutionNotes = resolutionNotes?.takeUnless { it.isBlank() },
                            )
                        _uiState.update { state ->
                            state.copy(
                                defaultClients = state.defaultClients.filterNot { it.id == request.id },
                                completedClients =
                                    state.completedClients
                                        .filterNot { it.id == request.id }
                                        .let { existing -> listOf(updatedRequest) + existing },
                                actionInProgressRequestId = null,
                                actionInProgressStatus = null,
                                actionCompletedAt = System.currentTimeMillis(),
                                actionErrorMessage = null,
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                actionInProgressRequestId = null,
                                actionInProgressStatus = null,
                                actionErrorMessage = error.message ?: "Failed to update client verification request",
                            )
                        }
                    },
                )
            }
        }
    }
