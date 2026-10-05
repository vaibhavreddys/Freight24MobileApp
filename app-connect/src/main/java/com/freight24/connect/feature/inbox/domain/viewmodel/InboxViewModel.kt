package com.freight24.connect.feature.inbox.domain.viewmodel

import com.freight24.rest.response.IndentInterestInboxResponse
import kotlinx.coroutines.flow.StateFlow

data class InboxUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val data: List<IndentInterestInboxResponse.IndentInterestInboxData> = emptyList(),
    val totalIndents: Int = 0,
)

sealed class InboxEvent {
    data object Refresh : InboxEvent()

    data class RejectInterest(
        val interestId: String,
        val remarks: String? = null,
    ) : InboxEvent()
}

interface InboxViewModel {
    val uiState: StateFlow<InboxUiState>

    fun onEvent(event: InboxEvent)
}
