package com.freight24.connect.feature.indents.domain.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.auth.SessionManager
import com.freight24.common.util.formatIndianCurrency
import com.freight24.connect.feature.findTrucks.domain.usecases.FindTrucksUseCase
import com.freight24.connect.feature.findTrucks.domain.usecases.FindTrucksUseCaseDataParams
import com.freight24.connect.feature.inbox.domain.usecase.WithDrawInterestUseCase
import com.freight24.connect.feature.inbox.domain.usecase.WithDrawInterestUseCaseParams
import com.freight24.connect.feature.indents.domain.usecases.AssignIndentUseCase
import com.freight24.connect.feature.indents.domain.usecases.AssignIndentUseCaseParams
import com.freight24.connect.feature.indents.domain.usecases.CancelIndentUseCase
import com.freight24.connect.feature.indents.domain.usecases.CancelIndentUseCaseParams
import com.freight24.connect.feature.indents.domain.usecases.GetIndentByIdUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetIndentByIdUseCaseParams
import com.freight24.connect.feature.indents.domain.usecases.TruckerInterestInboxUseCase
import com.freight24.connect.feature.indents.domain.usecases.TruckerInterestInboxUseCaseParams
import com.freight24.rest.response.IndentResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IndentDetailsViewModelImpl
    @Inject
    constructor(
        private val getIndentByIdUseCase: GetIndentByIdUseCase,
        private val assignIndentUseCase: AssignIndentUseCase,
        private val cancelIndentUseCase: CancelIndentUseCase,
        private val truckerInterestInboxUseCase: TruckerInterestInboxUseCase,
        private val withdrawInterestUseCase: WithDrawInterestUseCase,
        private val findTrucksUseCase: FindTrucksUseCase,
        sessionManager: SessionManager,
    ) : ViewModel(), IndentDetailsViewModel {
        private val _uiState = MutableStateFlow(IndentDetailsUiState())
        override val uiState: StateFlow<IndentDetailsUiState> = _uiState.asStateFlow()

        /**
         * Roles the backend exempts from record ownership on
         * `PUT /api/v1/indents/:id`. Resolved once — roles do not change mid-session.
         */
        private val canOverrideOwnership = sessionManager.isAdminOrOpsManager()

        private var currentIndentId: String? = null

        override fun onEvent(event: IndentDetailsEvent) {
            when (event) {
                is IndentDetailsEvent.LoadIndent -> {
                    currentIndentId = event.indentId
                    loadIndent(event.indentId)
                }
                is IndentDetailsEvent.Refresh -> {
                    currentIndentId?.let { loadIndent(it) }
                }
                is IndentDetailsEvent.PullToRefresh -> {
                    currentIndentId?.let { loadIndent(it, isPullToRefresh = true) }
                }
                is IndentDetailsEvent.ClearError -> {
                    _uiState.update { it.copy(errorMessage = null) }
                }
                is IndentDetailsEvent.ShowAssignDialog -> {
                    val indent = _uiState.value.indent
                    _uiState.update {
                        it.copy(
                            isAssignDialogVisible = true,
                            assignClientCost = indent?.indentGroupDetails?.clientTripCost?.toInt()?.toString()?.let(::formatIndianCurrency) ?: "",
                            assignNegotiatedCost = indent?.negotiatedTripCost?.toInt()?.toString()?.let(::formatIndianCurrency) ?: "",
                            assignNotes = "",
                            isNegotiatedSameAsClient = false,
                        )
                    }
                }
                is IndentDetailsEvent.HideAssignDialog -> {
                    _uiState.update { it.copy(isAssignDialogVisible = false) }
                }
                is IndentDetailsEvent.UpdateAssignField -> {
                    _uiState.update { state ->
                        val newClientCost = event.clientCost ?: state.assignClientCost
                        val newIsNegotiatedSame = event.isNegotiatedSameAsClient ?: state.isNegotiatedSameAsClient
                        val newNegotiatedCost =
                            if (newIsNegotiatedSame) {
                                newClientCost
                            } else {
                                event.negotiatedCost ?: state.assignNegotiatedCost
                            }

                        state.copy(
                            assignClientCost = newClientCost,
                            assignNegotiatedCost = newNegotiatedCost,
                            assignNotes = event.notes ?: state.assignNotes,
                            isNegotiatedSameAsClient = newIsNegotiatedSame,
                        )
                    }
                }
                is IndentDetailsEvent.ConfirmAssignment -> {
                    confirmAssignment()
                }
                is IndentDetailsEvent.ShowCancelDialog -> {
                    _uiState.update { it.copy(isCancelDialogVisible = true) }
                }
                is IndentDetailsEvent.HideCancelDialog -> {
                    _uiState.update { it.copy(isCancelDialogVisible = false) }
                }
                is IndentDetailsEvent.ConfirmCancel -> {
                    confirmCancel()
                }
                is IndentDetailsEvent.ShowTruckerSheet -> {
                    Log.d("FCM_DEBUG", "ShowTruckerSheet event received, opening sheet")
                    _uiState.update { it.copy(isTruckerSheetVisible = true) }
                    loadTruckerList()
                }
                is IndentDetailsEvent.HideTruckerSheet -> {
                    _uiState.update { it.copy(isTruckerSheetVisible = false) }
                }
                is IndentDetailsEvent.RejectTruckerInterest -> {
                    rejectTruckerInterest(event.interestId)
                }
                is IndentDetailsEvent.ShowSuitableTrucksSheet -> {
                    _uiState.update { it.copy(isSuitableTrucksSheetVisible = true) }
                    loadSuitableTrucks()
                }
                is IndentDetailsEvent.HideSuitableTrucksSheet -> {
                    _uiState.update { it.copy(isSuitableTrucksSheetVisible = false) }
                }
            }
        }

        private fun loadSuitableTrucks() {
            val indent = _uiState.value.indent ?: return
            val loadingLocation = indent.indentGroupDetails.indentLocations.find { it.locationType.equals("loading", true) }
            val unloadingLocation = indent.indentGroupDetails.indentLocations.find { it.locationType.equals("Unloading", true) }
            val truckType = indent.indentGroupDetails.truckType

            viewModelScope.launch {
                _uiState.update { it.copy(isSuitableTrucksLoading = true, errorMessage = null) }

                val params =
                    FindTrucksUseCaseDataParams(
                        areaId = loadingLocation?.areaId?.toString(),
                        cityId = loadingLocation?.cityId?.toString(),
                        destinationCityId = unloadingLocation?.cityId?.toString(),
                        truckTypeIds = truckType?.id ?: "",
                    )

                val result = findTrucksUseCase(params)
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isSuitableTrucksLoading = false,
                            suitableTrucksList = result.getOrNull(),
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSuitableTrucksLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to load suitable trucks",
                        )
                    }
                }
            }
        }

        private fun loadTruckerList() {
            val indentId = currentIndentId ?: return
            viewModelScope.launch {
                _uiState.update { it.copy(isTruckerListLoading = true, errorMessage = null) }
                val result = truckerInterestInboxUseCase(TruckerInterestInboxUseCaseParams(indentId))
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isTruckerListLoading = false,
                            truckerList = result.getOrNull()?.data ?: emptyList(),
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isTruckerListLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to load trucker list",
                        )
                    }
                }
            }
        }

        private fun rejectTruckerInterest(interestId: String) {
            viewModelScope.launch {
                _uiState.update { it.copy(isTruckerListLoading = true, errorMessage = null) }
                val result = withdrawInterestUseCase(WithDrawInterestUseCaseParams(interestId, null))
                if (result.isSuccess) {
                    loadTruckerList()
                    // Also refresh indent to update metrics if needed
                    currentIndentId?.let { loadIndent(it) }
                } else {
                    _uiState.update {
                        it.copy(
                            isTruckerListLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to reject interest",
                        )
                    }
                }
            }
        }

        /**
         * True when the backend's ownership type says this indent is the caller's —
         * `CREATED_BY_ME` or `CLONED_TO_ME`.
         *
         * A missing type is treated as not-theirs. The detail endpoint sends the
         * field on every response, so an absent value means the payload predates
         * it, and withholding the control is the safe reading: it only ever hides
         * an edit that would have been refused anyway.
         */
        private fun IndentResponse.Indent?.isOwnedByCaller(): Boolean =
            this?.indentOwnership?.let { !it.equals(OWNERSHIP_OTHER, ignoreCase = true) && it.isNotBlank() } == true

        private fun loadIndent(
            indentId: String,
            isPullToRefresh: Boolean = false,
        ) {
            viewModelScope.launch {
                // A pull-to-refresh must not blank the indent already on screen, so it
                // drives isRefreshing (the swipe indicator) instead of isLoading.
                _uiState.update {
                    if (isPullToRefresh) {
                        it.copy(isRefreshing = true)
                    } else {
                        it.copy(isLoading = true, errorMessage = null)
                    }
                }

                val result =
                    getIndentByIdUseCase.invoke(
                        GetIndentByIdUseCaseParams(identId = indentId),
                    )

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    val indent = response?.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            indent = indent,
                            canEdit = canOverrideOwnership || indent.isOwnedByCaller(),
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            // On refresh keep the stale indent visible; only surface the
                            // error when there is nothing to fall back to.
                            errorMessage =
                                if (isPullToRefresh && it.indent != null) {
                                    null
                                } else {
                                    result.exceptionOrNull()?.message ?: "Failed to load indent details"
                                },
                        )
                    }
                }
            }
        }

        private companion object {
            /** Backend `indentOwnership` value for an indent belonging to someone else. */
            const val OWNERSHIP_OTHER = "OTHER"
        }

        private fun confirmAssignment() {
            val indentId = currentIndentId ?: return
            val state = _uiState.value

            viewModelScope.launch {
                _uiState.update { it.copy(isAssigning = true, errorMessage = null) }

                val result =
                    assignIndentUseCase.invoke(
                        AssignIndentUseCaseParams(
                            id = indentId,
                            clientTripCost = state.assignClientCost.replace(",", "").toIntOrNull(),
                            negotiatedTripCost = state.assignNegotiatedCost.replace(",", "").toIntOrNull(),
                            notes = state.assignNotes,
                        ),
                    )

                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isAssigning = false,
                            isAssignDialogVisible = false,
                        )
                    }
                    loadIndent(indentId) // Refresh to get updated status
                } else {
                    _uiState.update {
                        it.copy(
                            isAssigning = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to assign indent",
                        )
                    }
                }
            }
        }

        private fun confirmCancel() {
            val indentId = currentIndentId ?: return

            viewModelScope.launch {
                _uiState.update { it.copy(isCanceling = true, errorMessage = null) }

                val result =
                    cancelIndentUseCase.invoke(
                        CancelIndentUseCaseParams(id = indentId),
                    )

                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isCanceling = false,
                            isCancelDialogVisible = false,
                        )
                    }
                    loadIndent(indentId) // Refresh to get updated status
                } else {
                    _uiState.update {
                        it.copy(
                            isCanceling = false,
                            isCancelDialogVisible = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to cancel indent",
                        )
                    }
                }
            }
        }
    }
