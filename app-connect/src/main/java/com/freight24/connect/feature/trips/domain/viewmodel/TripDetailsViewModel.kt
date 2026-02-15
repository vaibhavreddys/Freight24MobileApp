package com.freight24.connect.feature.trips.domain.viewmodel

import android.net.Uri
import com.freight24.rest.response.TripDataResponse
import kotlinx.coroutines.flow.StateFlow

data class TripDetailsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val trip: TripDataResponse? = null,
    val selectedUris: List<Uri> = emptyList(),
    val isPdf: Boolean = false,
    val isUploading: Boolean = false
)

sealed class TripDetailsEvent {
    data class LoadTrip(val tripId: String) : TripDetailsEvent()
    data class UpdateStatus(val newStatus: String) : TripDetailsEvent()
    data class OnFilesPicked(val uris: List<Uri>, val isPdf: Boolean) : TripDetailsEvent()
    data object OnClearSelection : TripDetailsEvent()
    data object UploadLR : TripDetailsEvent()
    data object Refresh : TripDetailsEvent()
}

interface TripDetailsViewModel {
    val uiState: StateFlow<TripDetailsUiState>
    fun onEvent(event: TripDetailsEvent)
}
