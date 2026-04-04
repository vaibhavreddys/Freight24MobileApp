package com.freight24.connect.feature.verificationrequests.domain.viewmodel

import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequest
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
    val loadingStatus: VerificationRequestStatus? = null,
    val actionInProgressRequestId: String? = null,
    val actionInProgressStatus: VerificationRequestStatus? = null,
    val actionCompletedAt: Long? = null,
    val loadedDefaultTrips: Boolean = false,
    val loadedCompletedTrips: Boolean = false,
) {
    val visibleTrips: List<VerificationRequest>
        get() = when (selectedStatus) {
            VerificationRequestStatus.COMPLETED -> completedTrips
            VerificationRequestStatus.APPROVED,
            VerificationRequestStatus.REJECTED -> completedTrips.filter { it.status == selectedStatus }
            VerificationRequestStatus.PENDING,
            VerificationRequestStatus.IN_REVIEW -> defaultTrips.filter { it.status == selectedStatus }
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
}

interface VerificationRequestsViewModel {
    val uiState: StateFlow<VerificationRequestsUiState>
    fun onEvent(event: VerificationRequestsEvent)
    fun getSignedUrl(s3Url: String, onUrlReady: (String) -> Unit)
}
