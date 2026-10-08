package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.compose.runtime.Immutable
import com.freight24.common.util.negotiatedCostExceedsClientError
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
    val canProgressIndent: Boolean = false,
    val isAssignDialogVisible: Boolean = false,
    val assignClientCost: String = "",
    val assignNegotiatedCost: String = "",
    val assignNotes: String = "",
    val isNegotiatedSameAsClient: Boolean = false,
    val isCancelDialogVisible: Boolean = false,
    val isReopenDialogVisible: Boolean = false,
    val isCanceling: Boolean = false,
    val lifecycleReasonCode: String = "",
    val lifecycleRemarks: String = "",
    val isTruckerSheetVisible: Boolean = false,
    val isTruckerListLoading: Boolean = false,
    val truckerList: List<TruckerInboxInterestResponse.TruckerInboxInterestInfo> = emptyList(),
    val isSuitableTrucksSheetVisible: Boolean = false,
    val isSuitableTrucksLoading: Boolean = false,
    val suitableTrucksList: com.freight24.rest.response.SearchTruckerResponse.TruckerData? = null,
    /**
     * Whether this operator may correct the indent — i.e. they raised it, it was
     * cloned to them, or they hold an admin / operations_manager role. Mirrors the
     * check the backend runs on `PUT /api/v1/indents/:id`, so the edit control is
     * withheld only where the write would actually be refused.
     */
    val canEdit: Boolean = false,
) {
    /** The client rate as a number, or null when the field is blank or half-typed. */
    val assignClientCostAmount: Int?
        get() = assignClientCost.replace(",", "").toIntOrNull()

    /** The negotiated rate as a number, or null when the field is blank or half-typed. */
    val assignNegotiatedCostAmount: Int?
        get() = assignNegotiatedCost.replace(",", "").toIntOrNull()

    /**
     * True when either rate is absent or not a positive amount.
     *
     * A blank field and a typed `0` mean the same thing here — the rate was never
     * agreed — and both must block. The backend would accept a zero: its
     * `isPositiveNumber` guard is `v >= 0` (backendapi/src/routes/indents.js:1826),
     * so `0` would sail through and publish an indent to the load board with no
     * rate on it. That is why this check lives here rather than being left to the
     * server.
     *
     * Note this is independent of [isNegotiatedSameAsClient]: that switch mirrors
     * the client rate into the negotiated field, so it resolves a missing negotiated
     * rate only once the client rate is itself positive.
     */
    val assignRatesMissing: Boolean
        get() = (assignClientCostAmount ?: 0) <= 0 || (assignNegotiatedCostAmount ?: 0) <= 0

    /** Which rates still need a value, for naming them in the operator's message. */
    val assignMissingRateLabels: List<String>
        get() = buildList {
            if ((assignClientCostAmount ?: 0) <= 0) add("client cost")
            if ((assignNegotiatedCostAmount ?: 0) <= 0) add("negotiated cost")
        }

    /**
     * The one rule for whether the indent may go live, read by both the sheet's
     * confirm button and the ViewModel's submit path so they cannot disagree.
     *
     * Mirrors [negotiatedCostExceedsClientError] rather than re-deriving the
     * comparison; that helper returns null when either side is unparseable, which
     * [assignRatesMissing] already covers.
     */
    val canConfirmAssignment: Boolean
        get() =
            !assignRatesMissing &&
                negotiatedCostExceedsClientError(assignNegotiatedCost, assignClientCost) == null
}

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

    object ShowReopenDialog : IndentDetailsEvent()

    object HideReopenDialog : IndentDetailsEvent()

    object ConfirmReopen : IndentDetailsEvent()

    data class UpdateLifecycleReason(val reasonCode: String) : IndentDetailsEvent()

    data class UpdateLifecycleRemarks(val remarks: String) : IndentDetailsEvent()

    object ShowTruckerSheet : IndentDetailsEvent()

    object HideTruckerSheet : IndentDetailsEvent()

    data class RejectTruckerInterest(val interestId: String) : IndentDetailsEvent()

    object ShowSuitableTrucksSheet : IndentDetailsEvent()

    object HideSuitableTrucksSheet : IndentDetailsEvent()
}
