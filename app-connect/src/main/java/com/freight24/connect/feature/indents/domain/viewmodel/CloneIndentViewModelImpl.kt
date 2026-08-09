package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.indents.domain.usecases.CloneIndentUseCase
import com.freight24.connect.feature.indents.domain.usecases.ClonedIndentUseCaseParams
import com.freight24.connect.feature.indents.domain.usecases.GetIndentByIdUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetIndentByIdUseCaseParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CloneIndentViewModelImpl @Inject constructor(
    private val getIndentByIdUseCase: GetIndentByIdUseCase,
    private val cloneIndentUseCase: CloneIndentUseCase
) : ViewModel(), CloneIndentViewModel {

    private val _uiState = MutableStateFlow(CloneIndentUiState())
    override val uiState: StateFlow<CloneIndentUiState> = _uiState.asStateFlow()

    private var currentIndentId: String? = null
    private var currentIndentGroupId: String? = null

    override fun onEvent(event: CloneIndentEvent) {
        when (event) {
            is CloneIndentEvent.LoadIndent -> {
                currentIndentId = event.indentId
                currentIndentGroupId = event.indentGroupId
                loadIndent(event.indentId)
            }
            is CloneIndentEvent.UpdateField -> {
                _uiState.update { state ->
                    state.copy(
                        assignedTo = event.assignedTo ?: state.assignedTo,
                        notes = event.notes ?: state.notes,
                        clientCost = event.clientCost ?: state.clientCost,
                        negotiatedCost = event.negotiatedCost ?: state.negotiatedCost,
                        businessProposedCost = event.businessProposedCost ?: state.businessProposedCost,
                        vehicleHeight = event.vehicleHeight ?: state.vehicleHeight,
                        flooringType = event.flooringType ?: state.flooringType,
                        tarpaulinRequired = event.tarpaulinRequired ?: state.tarpaulinRequired,
                        hideCost = event.hideCost ?: state.hideCost,
                        tatRequired = event.tatRequired ?: state.tatRequired,
                        tatHours = event.tatHours ?: state.tatHours,
                        remarks = event.remarks ?: state.remarks
                    )
                }
            }
            is CloneIndentEvent.SubmitClone -> {
                submitClone()
            }
            is CloneIndentEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
            is CloneIndentEvent.ClearSuccess -> _uiState.update { it.copy(successMessage = null, createdIndentId = null, createdIndentCode = null, createdDate = null) }
        }
    }

    private fun loadIndent(indentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = getIndentByIdUseCase.invoke(GetIndentByIdUseCaseParams(identId = indentId))
            if (result.isSuccess) {
                val indent = result.getOrNull()?.data
                if (indent != null) {
                    val details = indent.indentGroupDetails
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            indent = indent,
                            clientCost = details.clientTripCost?.toString() ?: "",
                            negotiatedCost = indent.negotiatedTripCost?.toString() ?: "",
                            businessProposedCost = details.businessProposedCost?.toString() ?: "",
                            vehicleHeight = details.vehicleHeight?.toString() ?: "",
                            flooringType = details.flooringType ?: "steel",
                            tarpaulinRequired = details.tarpaulingRequired,
                            hideCost = indent.hideCostInfo,
                            tatRequired = details.tatEnabled,
                            tatHours = details.tatHours?.toString() ?: "",
                            remarks = details.specialRequirements ?: ""
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to load indent"
                    )
                }
            }
        }
    }

    private fun submitClone() {
        val indentGroupId = currentIndentGroupId ?: return
        val currentState = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            val result = cloneIndentUseCase.invoke(
                ClonedIndentUseCaseParams(
                    groupIndentId = indentGroupId,
                    assignedTo = currentState.assignedTo.ifBlank { null },
                    notes = currentState.notes.ifBlank { null }
                )
            )

            if (result.isSuccess) {
                val response = result.getOrNull()
                val createdIndent = response?.data
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        successMessage = "Indent cloned successfully",
                        createdIndentId = createdIndent?.id,
                        createdIndentCode = createdIndent?.indentCode,
                        createdDate = createdIndent?.createdAt,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to clone indent"
                    )
                }
            }
        }
    }
}
