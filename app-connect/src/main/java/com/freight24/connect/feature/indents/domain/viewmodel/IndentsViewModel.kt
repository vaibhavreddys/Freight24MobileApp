package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.compose.runtime.Immutable
import com.freight24.rest.response.ClientDto
import com.freight24.rest.response.LocationDto
import com.freight24.rest.response.TruckCategoryDto
import com.freight24.rest.response.TruckTypeDto
import kotlinx.coroutines.flow.StateFlow

interface IndentsViewModel {

    val uiState: StateFlow<CreateIndentUiState>

    fun onEvent(event: CreateIndentEvent)
}

@Immutable
data class CreateIndentUiState(
    val fromLocationQuery: String = "",
    val fromLocationSearchResults: List<LocationDto> = emptyList(),
    val selectedFromLocation: LocationDto? = null,
    val toLocationQuery: String = "",
    val toLocationSearchResults: List<LocationDto> = emptyList(),
    val selectedToLocation: LocationDto? = null,
    val clientQuery: String = "",
    val clientSearchResults: List<ClientDto> = emptyList(),
    val selectedClient: ClientDto? = null,
    val materialName: String = "",
    val weight: String = "",
    val truckTypes: TruckCategoryDto =
        TruckCategoryDto(
            emptyList(),
            emptyList(),
            emptyList(),
        ),
    val isLoadingTruckTypes: Boolean = false,
    val selectedTruckType: TruckTypeDto? = null,
    // Optional
    val clientCost: String = "",
    val negotiatedCost: String = "",
    val businessProposedCost: String = "",
    val vehicleHeight: String = "",
    val flooringType: String = "Steel", // Default
    val tatRequired: Boolean = false,
    val tatHours: String = "",
    val tarpaulinRequired: Boolean = false,
    val hideCost: Boolean = false,
    val remarks: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val createdIndentId: String? = null,
)

sealed class CreateIndentEvent {
    data class SearchFromLocation(val query: String) : CreateIndentEvent()

    data class SearchToLocation(val query: String) : CreateIndentEvent()

    data class SearchClient(val query: String) : CreateIndentEvent()

    data class SelectFromLocation(val location: LocationDto) : CreateIndentEvent()

    data class SelectToLocation(val location: LocationDto) : CreateIndentEvent()

    data class SelectClient(val client: ClientDto) : CreateIndentEvent()

    data class UpdateMaterialName(val name: String) : CreateIndentEvent()

    data class UpdateWeight(val weight: String) : CreateIndentEvent()

    data class SelectTruckType(val truckType: TruckTypeDto) : CreateIndentEvent()

    data class UpdateOptionalField(
        val clientCost: String? = null,
        val negotiatedCost: String? = null,
        val businessProposedCost: String? = null,
        val vehicleHeight: String? = null,
        val flooringType: String? = null,
        val tatRequired: Boolean? = null,
        val tarpaulinRequired: Boolean? = null,
        val hideCost: Boolean? = null,
        val remarks: String? = null,
        val tatHours: String? = null,
    ) : CreateIndentEvent()

    object SubmitIndent : CreateIndentEvent()

    object ClearError : CreateIndentEvent()

    object ClearSuccess : CreateIndentEvent()
}
