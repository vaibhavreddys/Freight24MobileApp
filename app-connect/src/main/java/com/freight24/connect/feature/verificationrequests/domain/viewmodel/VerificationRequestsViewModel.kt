package com.freight24.connect.feature.verificationrequests.domain.viewmodel

import com.freight24.connect.feature.verificationrequests.domain.model.ClientVerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.TruckOwnerVerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.TruckVerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestDocument
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestStatus
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestTab
import kotlinx.coroutines.flow.StateFlow

data class VerificationRequestsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val actionErrorMessage: String? = null,
    val selectedTab: VerificationRequestTab = VerificationRequestTab.TRIPS,
    val selectedStatus: VerificationRequestStatus = VerificationRequestStatus.PENDING,
    val defaultTrips: List<VerificationRequest> = emptyList(),
    val completedTrips: List<VerificationRequest> = emptyList(),
    val defaultTrucks: List<TruckVerificationRequest> = emptyList(),
    val completedTrucks: List<TruckVerificationRequest> = emptyList(),
    val defaultTruckOwners: List<TruckOwnerVerificationRequest> = emptyList(),
    val completedTruckOwners: List<TruckOwnerVerificationRequest> = emptyList(),
    val defaultClients: List<ClientVerificationRequest> = emptyList(),
    val completedClients: List<ClientVerificationRequest> = emptyList(),
    val loadingStatus: VerificationRequestStatus? = null,
    val actionInProgressRequestId: String? = null,
    val actionInProgressStatus: VerificationRequestStatus? = null,
    val actionCompletedAt: Long? = null,
    val loadedDefaultTrips: Boolean = false,
    val loadedCompletedTrips: Boolean = false,
    val loadedDefaultTrucks: Boolean = false,
    val loadedCompletedTrucks: Boolean = false,
    val loadedDefaultTruckOwners: Boolean = false,
    val loadedCompletedTruckOwners: Boolean = false,
    val loadedDefaultClients: Boolean = false,
    val loadedCompletedClients: Boolean = false,
) {
    val visibleTrips: List<VerificationRequest>
        get() =
            when (selectedStatus) {
                VerificationRequestStatus.COMPLETED -> completedTrips
                VerificationRequestStatus.APPROVED,
                VerificationRequestStatus.REJECTED,
                VerificationRequestStatus.VERIFIED,
                -> completedTrips.filter { it.status == selectedStatus }
                VerificationRequestStatus.PENDING,
                VerificationRequestStatus.IN_REVIEW,
                -> defaultTrips.filter { it.status == selectedStatus }
            }

    val visibleTrucks: List<TruckVerificationRequest>
        get() =
            when (selectedStatus) {
                VerificationRequestStatus.COMPLETED -> completedTrucks
                VerificationRequestStatus.PENDING,
                VerificationRequestStatus.IN_REVIEW,
                -> defaultTrucks.filter { it.status == selectedStatus }
                else -> emptyList()
            }

    val visibleTruckOwners: List<TruckOwnerVerificationRequest>
        get() =
            when (selectedStatus) {
                VerificationRequestStatus.COMPLETED -> completedTruckOwners
                VerificationRequestStatus.PENDING,
                VerificationRequestStatus.IN_REVIEW,
                -> defaultTruckOwners.filter { it.status == selectedStatus }
                else -> emptyList()
            }

    val visibleClients: List<ClientVerificationRequest>
        get() =
            when (selectedStatus) {
                VerificationRequestStatus.COMPLETED -> completedClients
                VerificationRequestStatus.PENDING,
                VerificationRequestStatus.IN_REVIEW,
                -> defaultClients.filter { it.status == selectedStatus }
                else -> emptyList()
            }
}

sealed class VerificationRequestsEvent {
    data class SelectTab(val tab: VerificationRequestTab) : VerificationRequestsEvent()

    data class SelectStatus(val status: VerificationRequestStatus) : VerificationRequestsEvent()

    data object Refresh : VerificationRequestsEvent()

    data object ClearActionState : VerificationRequestsEvent()

    data class UpdateTripStatus(
        val request: VerificationRequest,
        val status: VerificationRequestStatus,
        val resolutionNotes: String?,
    ) : VerificationRequestsEvent()

    data class UpdateTruckStatus(
        val request: TruckVerificationRequest,
        val status: VerificationRequestStatus,
        val resolutionNotes: String?,
    ) : VerificationRequestsEvent()

    data class UpdateTruckOwnerStatus(
        val request: TruckOwnerVerificationRequest,
        val status: VerificationRequestStatus,
        val resolutionNotes: String?,
    ) : VerificationRequestsEvent()

    data class UpdateClientStatus(
        val request: ClientVerificationRequest,
        val status: VerificationRequestStatus,
        val resolutionNotes: String?,
    ) : VerificationRequestsEvent()
}

interface VerificationRequestsViewModel {
    val uiState: StateFlow<VerificationRequestsUiState>

    fun onEvent(event: VerificationRequestsEvent)

    fun getSignedUrl(
        document: VerificationRequestDocument,
        onUrlReady: (String) -> Unit,
    )
}
