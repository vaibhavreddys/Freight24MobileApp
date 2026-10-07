package com.freight24.connect.feature.trips.domain.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.util.FileUtils
import com.freight24.connect.feature.trips.domain.usecase.CancelTripUseCase
import com.freight24.connect.feature.trips.domain.usecase.CancelTripUseCaseDataParams
import com.freight24.connect.feature.trips.domain.usecase.GetPaymentTransactionsParams
import com.freight24.connect.feature.trips.domain.usecase.GetPaymentTransactionsUseCase
import com.freight24.connect.feature.trips.domain.usecase.LoadTripDocumentFile
import com.freight24.connect.feature.trips.domain.usecase.TripDetailsUseCase
import com.freight24.connect.feature.trips.domain.usecase.TripDetailsUseCaseDataParams
import com.freight24.connect.feature.trips.domain.usecase.UpdateTruckerUseCase
import com.freight24.connect.feature.trips.domain.usecase.UpdateTruckerUseCaseDataParams
import com.freight24.connect.feature.trips.domain.usecase.UploadLrUseCase
import com.freight24.connect.feature.trips.domain.usecase.UploadLrUseCaseDataParams
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TripDetailsViewModelImpl
    @Inject
    constructor(
        private val tripDetailsUseCase: TripDetailsUseCase,
        private val updateTruckerUseCase: UpdateTruckerUseCase,
        private val uploadLrUseCase: UploadLrUseCase,
        private val cancelTripUseCase: CancelTripUseCase,
        private val loadTripDocumentFile: LoadTripDocumentFile,
        private val getPaymentTransactionsUseCase: GetPaymentTransactionsUseCase,
        @param:ApplicationContext private val context: Context,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel(), TripDetailsViewModel {
        private val tripId: String = checkNotNull(savedStateHandle["tripId"])

        private val _uiState = MutableStateFlow(TripDetailsUiState())
        override val uiState: StateFlow<TripDetailsUiState> = _uiState.asStateFlow()
        private var paymentTransactionsJob: Job? = null

        init {
            loadTrip()
        }

        override fun onEvent(event: TripDetailsEvent) {
            when (event) {
                is TripDetailsEvent.LoadTrip -> {
                    // Usually tripId is fixed from navigation, but supporting re-load if needed
                    loadTrip(event.tripId)
                }
                is TripDetailsEvent.UpdateStatus -> {
                    updateStatus(event.newStatus)
                }
                is TripDetailsEvent.OnFilesPicked -> {
                    _uiState.update { it.copy(selectedUris = event.uris, isPdf = event.isPdf) }
                }
                TripDetailsEvent.OnClearSelection -> {
                    _uiState.update { it.copy(selectedUris = emptyList(), isPdf = false) }
                }
                TripDetailsEvent.UploadLR -> {
                    uploadLR()
                }
                TripDetailsEvent.Refresh -> loadTrip()
                TripDetailsEvent.PullToRefresh -> loadTrip(isPullToRefresh = true)
                is TripDetailsEvent.ShareDocument -> shareDocument(event.documentId)
                TripDetailsEvent.DocumentShareHandled -> {
                    _uiState.update { it.copy(sharedDocumentFile = null, documentShareFailed = false, sharingDocumentId = null) }
                }
                TripDetailsEvent.ShowCancelDialog -> {
                    val status = _uiState.value.trip?.truckerStatus?.uppercase() ?: ""
                    if (status == "ASSIGNED" || status == "LOADING") {
                        _uiState.update { it.copy(isCancelDialogVisible = true) }
                    } else {
                        val readableStatus = _uiState.value.trip?.truckerStatus?.replace("_", " ") ?: status
                        _uiState.update { it.copy(cancelNotAllowedMessage = readableStatus) }
                    }
                }
                TripDetailsEvent.HideCancelDialog -> {
                    _uiState.update { it.copy(isCancelDialogVisible = false, cancelReason = "") }
                }
                TripDetailsEvent.ConfirmCancelTrip -> {
                    cancelTrip()
                }
                TripDetailsEvent.DismissCancelNotAllowed -> {
                    _uiState.update { it.copy(cancelNotAllowedMessage = null) }
                }
                is TripDetailsEvent.UpdateCancelReason -> {
                    _uiState.update { it.copy(cancelReason = event.reason) }
                }
                is TripDetailsEvent.ViewPaymentTransactions -> loadPaymentTransactions(event.userType)
                TripDetailsEvent.RetryPaymentTransactions -> {
                    _uiState.value.transactionUserType?.let(::loadPaymentTransactions)
                }
                TripDetailsEvent.DismissPaymentTransactions -> {
                    paymentTransactionsJob?.cancel()
                    _uiState.update {
                        it.copy(
                            transactionUserType = null,
                            transactions = emptyList(),
                            isTransactionsLoading = false,
                            transactionsError = null,
                        )
                    }
                }
            }
        }

        private fun loadPaymentTransactions(userType: String) {
            paymentTransactionsJob?.cancel()
            paymentTransactionsJob =
                viewModelScope.launch {
                    _uiState.update {
                        it.copy(
                            transactionUserType = userType,
                            transactions = emptyList(),
                            isTransactionsLoading = true,
                            transactionsError = null,
                        )
                    }
                    getPaymentTransactionsUseCase(GetPaymentTransactionsParams(tripId, userType)).fold(
                        onSuccess = { transactions ->
                            _uiState.update {
                                it.copy(transactions = transactions, isTransactionsLoading = false)
                            }
                        },
                        onFailure = { error ->
                            _uiState.update {
                                it.copy(
                                    isTransactionsLoading = false,
                                    transactionsError = error.message ?: "Unable to load transactions",
                                )
                            }
                        },
                    )
                }
        }

        private fun shareDocument(documentId: String) {
            if (_uiState.value.sharingDocumentId != null) return
            val document =
                _uiState.value.trip?.documents
                    ?.firstOrNull { it.id == documentId } ?: return
            _uiState.update {
                it.copy(sharingDocumentId = document.id, sharedDocumentType = document.type, documentShareFailed = false)
            }
            viewModelScope.launch {
                val result = loadTripDocumentFile(tripId, document.id)
                _uiState.update {
                    it.copy(
                        sharedDocumentFile = result.getOrNull(),
                        documentShareFailed = result.isFailure,
                    )
                }
            }
        }

        private fun cancelTrip() {
            viewModelScope.launch {
                _uiState.update { it.copy(isCanceling = true) }
                val result = cancelTripUseCase(CancelTripUseCaseDataParams(tripId = tripId, reason = _uiState.value.cancelReason.trim()))
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isCanceling = false, isCancelDialogVisible = false, cancelReason = "") }
                        loadTrip()
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isCanceling = false,
                                isCancelDialogVisible = false,
                                cancelReason = "",
                                errorMessage = error.message ?: "Cancellation failed",
                            )
                        }
                    },
                )
            }
        }

        private fun updateStatus(newStatus: String) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val result =
                    updateTruckerUseCase(
                        UpdateTruckerUseCaseDataParams(
                            tripId = tripId,
                            truckerStatus = newStatus,
                            remarks = "Status updated via Mobile App",
                        ),
                    )

                result.fold(
                    onSuccess = { response ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                trip = response,
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Update failed",
                            )
                        }
                    },
                )
            }
        }

        private fun uploadLR() {
            val uris = _uiState.value.selectedUris
            if (uris.isEmpty()) return

            viewModelScope.launch {
                _uiState.update { it.copy(isUploading = true, errorMessage = null) }

                try {
                    val file =
                        if (_uiState.value.isPdf) {
                            FileUtils.getFileFromUri(context, uris.first())
                        } else {
                            FileUtils.convertImageToPdf(context, uris)
                        }

                    if (file != null) {
                        val result =
                            uploadLrUseCase(
                                UploadLrUseCaseDataParams(
                                    tripId = tripId,
                                    file = file,
                                    notes = "LR uploaded via Mobile App",
                                ),
                            )

                        result.fold(
                            onSuccess = { tripResponse ->
                                _uiState.update {
                                    it.copy(
                                        isUploading = false,
                                        selectedUris = emptyList(),
                                        isPdf = false,
                                        trip = tripResponse,
                                    )
                                }
                            },
                            onFailure = { error ->
                                _uiState.update {
                                    it.copy(
                                        isUploading = false,
                                        errorMessage = error.message ?: "Upload failed",
                                    )
                                }
                            },
                        )
                    } else {
                        _uiState.update { it.copy(isUploading = false, errorMessage = "Failed to process file") }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isUploading = false, errorMessage = e.message ?: "An error occurred") }
                }
            }
        }

        private fun loadTrip(
            id: String = tripId,
            isPullToRefresh: Boolean = false,
        ) {
            viewModelScope.launch {
                // A pull-to-refresh must not blank the trip already on screen, so it
                // drives isRefreshing (the swipe indicator) instead of isLoading.
                _uiState.update {
                    if (isPullToRefresh) {
                        it.copy(isRefreshing = true)
                    } else {
                        it.copy(isLoading = true, errorMessage = null)
                    }
                }

                val result = tripDetailsUseCase(TripDetailsUseCaseDataParams(tripId = id))

                result.fold(
                    onSuccess = { response ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                trip = response,
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                // On refresh keep the stale trip visible; only surface the
                                // error when there is nothing to fall back to.
                                errorMessage = if (isPullToRefresh && it.trip != null) null else error.message ?: "Unknown error",
                            )
                        }
                    },
                )
            }
        }
    }
