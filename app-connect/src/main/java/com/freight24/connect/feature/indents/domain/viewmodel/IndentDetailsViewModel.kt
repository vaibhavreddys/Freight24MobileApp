package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.compose.runtime.Immutable
import com.freight24.rest.response.IndentResponse
import com.freight24.rest.response.TruckerInboxInterestResponse
import kotlinx.coroutines.flow.StateFlow

interface IndentDetailsViewModel {
    val uiState: StateFlow<IndentDetailsUiState>

    fun onEvent(event: IndentDetailsEvent)
}

@Immutable
data class IndentDetailsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isAssigning: Boolean = false,
    val errorMessage: String? = null,
    val indent: IndentResponse.Indent? = null,
    val isAssignDialogVisible: Boolean = false,
    val assignClientCost: String = "",
    val assignNegotiatedCost: String = "",
    val assignNotes: String = "",
    val isNegotiatedSameAsClient: Boolean = false,
    val isCancelDialogVisible: Boolean = false,
    val isCanceling: Boolean = false,
    val isTruckerSheetVisible: Boolean = false,
    val isTruckerListLoading: Boolean = false,
    val truckerList: List<TruckerInboxInterestResponse.TruckerInboxInterestInfo> = emptyList(),
    val isSuitableTrucksSheetVisible: Boolean = false,
    val isSuitableTrucksLoading: Boolean = false,
    val suitableTrucksList: com.freight24.rest.response.SearchTruckerResponse.TruckerData? = null,
)

sealed class IndentDetailsEvent {
    data class LoadIndent(val indentId: String) : IndentDetailsEvent()

    object Refresh : IndentDetailsEvent()

    /** Pull-to-refresh; keeps the loaded indent on screen instead of showing the full-page loader. */
    object PullToRefresh : IndentDetailsEvent()

    object ClearError : IndentDetailsEvent()

    object ShowAssignDialog : IndentDetailsEvent()

    object HideAssignDialog : IndentDetailsEvent()

    data class UpdateAssignField(
        val clientCost: String? = null,
        val negotiatedCost: String? = null,
        val notes: String? = null,
        val isNegotiatedSameAsClient: Boolean? = null,
    ) : IndentDetailsEvent()

    object ConfirmAssignment : IndentDetailsEvent()

    object ShowCancelDialog : IndentDetailsEvent()

    object HideCancelDialog : IndentDetailsEvent()

    object ConfirmCancel : IndentDetailsEvent()

    object ShowTruckerSheet : IndentDetailsEvent()

    object HideTruckerSheet : IndentDetailsEvent()

    data class RejectTruckerInterest(val interestId: String) : IndentDetailsEvent()

    object ShowSuitableTrucksSheet : IndentDetailsEvent()

    object HideSuitableTrucksSheet : IndentDetailsEvent()
}
