package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.indents.domain.usecases.GetIndentByIdUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetIndentByIdUseCaseParams
import com.freight24.connect.feature.indents.domain.usecases.UpdateIndentUseCase
import com.freight24.connect.feature.indents.domain.usecases.UpdateIndentUseCaseParams
import com.freight24.common.util.formatIndianCurrency
import com.freight24.rest.request.CreateIndentRequest
import com.freight24.rest.request.UpdateIndentRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditIndentViewModelImpl @Inject constructor(
    private val getIndentByIdUseCase: GetIndentByIdUseCase,
    private val updateIndentUseCase: UpdateIndentUseCase
) : ViewModel(), EditIndentViewModel {

    private val _uiState = MutableStateFlow(EditIndentUiState())
    override val uiState: StateFlow<EditIndentUiState> = _uiState.asStateFlow()

    private var currentIndentId: String? = null

    override fun onEvent(event: EditIndentEvent) {
        when (event) {
            is EditIndentEvent.LoadIndent -> {
                currentIndentId = event.indentId
                loadIndent(event.indentId)
            }
            is EditIndentEvent.UpdateField -> {
                _uiState.update { state ->
                    val next = state.copy(
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
                    next.copy(hasChanges = computeHasChanges(next))
                }
            }
            is EditIndentEvent.SubmitUpdate -> {
                submitUpdate()
            }
            is EditIndentEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
            is EditIndentEvent.ClearSuccess -> _uiState.update { it.copy(successMessage = null) }
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
                            hasChanges = false,
                            clientCost = details.clientTripCost?.toInt()?.toString()?.let(::formatIndianCurrency) ?: "",
                            negotiatedCost = indent.negotiatedTripCost?.toInt()?.toString()?.let(::formatIndianCurrency) ?: "",
                            businessProposedCost = details.businessProposedCost?.toInt()?.toString()?.let(::formatIndianCurrency) ?: "",
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

    private fun submitUpdate() {
        val indentId = currentIndentId ?: return
        val originalIndent = _uiState.value.indent ?: return
        val originalDetails = originalIndent.indentGroupDetails
        val currentState = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            val request = UpdateIndentRequest(
                clientTripCost = compareFields(currentState.clientCost.replace(",", "").toIntOrNull(), originalDetails.clientTripCost?.toInt()),
                negotiatedTripCost = compareFields(currentState.negotiatedCost.replace(",", "").toIntOrNull(), originalIndent.negotiatedTripCost?.toInt()),
                businessProposedCost = compareFields(currentState.businessProposedCost.replace(",", "").toIntOrNull(), originalDetails.businessProposedCost?.toInt()),
                vehicleHeight = compareFields(currentState.vehicleHeight.toFloatOrNull(), originalDetails.vehicleHeight?.toFloat()),
                hideCostInfo = compareFields(currentState.hideCost, originalIndent.hideCostInfo),
                tatEnabled = compareFields(currentState.tatRequired, originalDetails.tatEnabled),
                tatHours = compareFields(currentState.tatHours.toIntOrNull(), originalDetails.tatHours),
                tarpaulingRequired = compareFields(currentState.tarpaulinRequired, originalDetails.tarpaulingRequired),
                remarks = compareFields(currentState.remarks, originalDetails.specialRequirements),
                flooringType = compareFields(currentState.flooringType, originalDetails.flooringType)
            )

            // If no fields changed, just succeed immediately
            if (isRequestEmpty(request)) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        successMessage = "No changes to update"
                    )
                }
                return@launch
            }

            val result = updateIndentUseCase.invoke(UpdateIndentUseCaseParams(id = indentId, request = request))
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        successMessage = "Indent updated successfully"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to update indent"
                    )
                }
            }
        }
    }

    private fun computeHasChanges(state: EditIndentUiState): Boolean {
        val original = state.indent?.indentGroupDetails ?: return false
        val originalIndent = state.indent
        return compareFields(state.clientCost.replace(",", "").toIntOrNull(), original.clientTripCost?.toInt()) != null ||
                compareFields(state.negotiatedCost.replace(",", "").toIntOrNull(), originalIndent?.negotiatedTripCost?.toInt()) != null ||
                compareFields(state.businessProposedCost.replace(",", "").toIntOrNull(), original.businessProposedCost?.toInt()) != null ||
                compareFields(state.vehicleHeight.toFloatOrNull(), original.vehicleHeight?.toFloat()) != null ||
                compareFields(state.flooringType, original.flooringType) != null ||
                compareFields(state.tarpaulinRequired, original.tarpaulingRequired) != null ||
                compareFields(state.hideCost, originalIndent?.hideCostInfo) != null ||
                compareFields(state.tatRequired, original.tatEnabled) != null ||
                compareFields(state.tatHours.toIntOrNull(), original.tatHours) != null ||
                compareFields(state.remarks, original.specialRequirements) != null
    }

    private fun <T> compareFields(current: T?, original: T?): T? {
        return if (current == original) null else current
    }

    private fun isRequestEmpty(request: UpdateIndentRequest): Boolean {
        return request.clientTripCost == null &&
                request.negotiatedTripCost == null &&
                request.businessProposedCost == null &&
                request.vehicleHeight == null &&
                request.hideCostInfo == null &&
                request.tatEnabled == null &&
                request.tatHours == null &&
                request.tarpaulingRequired == null &&
                request.remarks == null &&
                request.flooringType == null &&
                request.status == null &&
                request.notes == null &&
                request.luIncludedInTripCost == null
    }
}
