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
    val errorMessage: String? = null,
    val indent: IndentResponse.Indent? = null
)

sealed class IndentDetailsEvent {
    data class LoadIndent(val indentId: String) : IndentDetailsEvent()
    object Refresh : IndentDetailsEvent()
    object ClearError : IndentDetailsEvent()
}
