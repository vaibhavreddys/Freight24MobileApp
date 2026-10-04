package com.freight24.connect.feature.trips.domain.viewmodel

import android.net.Uri
import com.freight24.rest.response.TripDataResponse
import com.freight24.rest.response.PaymentTransaction
import kotlinx.coroutines.flow.StateFlow
import java.io.File

data class TripDetailsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val trip: TripDataResponse? = null,
    val selectedUris: List<Uri> = emptyList(),
    val isPdf: Boolean = false,
    val isUploading: Boolean = false,
    val isCancelDialogVisible: Boolean = false,
    val isCanceling: Boolean = false,
    val cancelReason: String = "",
    val cancelNotAllowedMessage: String? = null,
    val sharingDocumentId: String? = null,
    val sharedDocumentType: String? = null,
    val sharedDocumentFile: File? = null,
    val documentShareFailed: Boolean = false,
    val transactionUserType: String? = null,
    val transactions: List<PaymentTransaction> = emptyList(),
    val isTransactionsLoading: Boolean = false,
    val transactionsError: String? = null
)

sealed class TripDetailsEvent {
    data class LoadTrip(val tripId: String) : TripDetailsEvent()
    data class UpdateStatus(val newStatus: String) : TripDetailsEvent()
    data class OnFilesPicked(val uris: List<Uri>, val isPdf: Boolean) : TripDetailsEvent()
    data object OnClearSelection : TripDetailsEvent()
    data object UploadLR : TripDetailsEvent()
    data object Refresh : TripDetailsEvent()
    data class ShareDocument(val documentId: String) : TripDetailsEvent()
    data object DocumentShareHandled : TripDetailsEvent()
    data object ShowCancelDialog : TripDetailsEvent()
    data object HideCancelDialog : TripDetailsEvent()
    data object ConfirmCancelTrip : TripDetailsEvent()
    data object DismissCancelNotAllowed : TripDetailsEvent()
    data class UpdateCancelReason(val reason: String) : TripDetailsEvent()
    data class ViewPaymentTransactions(val userType: String) : TripDetailsEvent()
    data object RetryPaymentTransactions : TripDetailsEvent()
    data object DismissPaymentTransactions : TripDetailsEvent()
}

interface TripDetailsViewModel {
    val uiState: StateFlow<TripDetailsUiState>
    fun onEvent(event: TripDetailsEvent)
}
