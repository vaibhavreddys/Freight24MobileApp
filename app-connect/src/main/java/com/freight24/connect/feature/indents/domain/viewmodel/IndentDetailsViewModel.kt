package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.compose.runtime.Immutable
import com.freight24.rest.response.IndentResponse
import kotlinx.coroutines.flow.StateFlow

interface IndentDetailsViewModel {
    val uiState: StateFlow<IndentDetailsUiState>
    fun onEvent(event: IndentDetailsEvent)
}

@Immutable
data class IndentDetailsUiState(
    val isLoading: Boolean = false,
    val isAssigning: Boolean = false,
    val errorMessage: String? = null,
    val indent: IndentResponse.Indent? = null,
    val isAssignDialogVisible: Boolean = false,
    val assignClientCost: String = "",
    val assignNegotiatedCost: String = "",
    val assignNotes: String = "",
    val isNegotiatedSameAsClient: Boolean = false
)

sealed class IndentDetailsEvent {
    data class LoadIndent(val indentId: String) : IndentDetailsEvent()
    object Refresh : IndentDetailsEvent()
    object ClearError : IndentDetailsEvent()
    object ShowAssignDialog : IndentDetailsEvent()
    object HideAssignDialog : IndentDetailsEvent()
    data class UpdateAssignField(
        val clientCost: String? = null,
        val negotiatedCost: String? = null,
        val notes: String? = null,
        val isNegotiatedSameAsClient: Boolean? = null
    ) : IndentDetailsEvent()
    object ConfirmAssignment : IndentDetailsEvent()
}
