package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.indents.domain.usecases.AssignIndentUseCase
import com.freight24.connect.feature.indents.domain.usecases.AssignIndentUseCaseParams
import com.freight24.connect.feature.indents.domain.usecases.GetIndentByIdUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetIndentByIdUseCaseParams
import com.freight24.connect.feature.indents.domain.usecases.CancelIndentUseCase
import com.freight24.connect.feature.indents.domain.usecases.CancelIndentUseCaseParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IndentDetailsViewModelImpl @Inject constructor(
    private val getIndentByIdUseCase: GetIndentByIdUseCase,
    private val assignIndentUseCase: AssignIndentUseCase,
    private val cancelIndentUseCase: CancelIndentUseCase
) : ViewModel(), IndentDetailsViewModel {

    private val _uiState = MutableStateFlow(IndentDetailsUiState())
    override val uiState: StateFlow<IndentDetailsUiState> = _uiState.asStateFlow()

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
            is IndentDetailsEvent.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            is IndentDetailsEvent.ShowAssignDialog -> {
                val indent = _uiState.value.indent
                _uiState.update {
                    it.copy(
                        isAssignDialogVisible = true,
                        assignClientCost = indent?.indentGroupDetails?.clientTripCost?.toInt()?.toString() ?: "",
                        assignNegotiatedCost = indent?.negotiatedTripCost?.toInt()?.toString() ?: "",
                        assignNotes = "",
                        isNegotiatedSameAsClient = false
                    )
                }
            }
            is IndentDetailsEvent.HideAssignDialog -> {
                _uiState.update { it.copy(isAssignDialogVisible = false) }
            }
            is IndentDetailsEvent.UpdateAssignField -> {
                _uiState.update { state ->
                    val newClientCost = event.clientCost ?: state.assignClientCost
                    val newIsNegotiatedSame = event.isNegotiatedSameAsClient ?: false
                    val newNegotiatedCost = if (newIsNegotiatedSame) {
                        newClientCost
                    } else {
                        event.negotiatedCost ?: state.indent?.negotiatedTripCost?.toInt()?.toString() ?: ""
                    }

                    state.copy(
                        assignClientCost = newClientCost,
                        assignNegotiatedCost = newNegotiatedCost,
                        assignNotes = event.notes ?: state.assignNotes,
                        isNegotiatedSameAsClient = newIsNegotiatedSame
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
        }
    }

    private fun loadIndent(indentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = getIndentByIdUseCase.invoke(
                GetIndentByIdUseCaseParams(identId = indentId)
            )

            if (result.isSuccess) {
                val response = result.getOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        indent = response?.data
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to load indent details"
                    )
                }
            }
        }
    }

    private fun confirmAssignment() {
        val indentId = currentIndentId ?: return
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isAssigning = true, errorMessage = null) }

            val result = assignIndentUseCase.invoke(
                AssignIndentUseCaseParams(
                    id = indentId,
                    clientTripCost = state.assignClientCost.toIntOrNull(),
                    negotiatedTripCost = state.assignNegotiatedCost.toIntOrNull(),
                    notes = state.assignNotes
                )
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isAssigning = false,
                        isAssignDialogVisible = false
                    )
                }
                loadIndent(indentId) // Refresh to get updated status
            } else {
                _uiState.update {
                    it.copy(
                        isAssigning = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to assign indent"
                    )
                }
            }
        }
    }

    private fun confirmCancel() {
        val indentId = currentIndentId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isCanceling = true, errorMessage = null) }

            val result = cancelIndentUseCase.invoke(
                CancelIndentUseCaseParams(id = indentId)
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isCanceling = false,
                        isCancelDialogVisible = false
                    )
                }
                loadIndent(indentId) // Refresh to get updated status
            } else {
                _uiState.update {
                    it.copy(
                        isCanceling = false,
                        isCancelDialogVisible = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to cancel indent"
                    )
                }
            }
        }
    }
}
