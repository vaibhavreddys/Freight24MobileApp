package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.compose.runtime.Immutable
import com.freight24.rest.response.IndentResponse
import kotlinx.coroutines.flow.StateFlow

interface SearchIndentsViewModel {
    val uiState: StateFlow<SearchIndentsUiState>
    fun onEvent(event: SearchIndentsEvent)
}

@Immutable
data class SearchIndentsUiState(
    val query: String = "",
    val indents: List<IndentResponse.Indent> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Filters
    val priority: String? = null,
    val clientId: String? = null,
    val assignedToMe: Boolean = false
)

sealed class SearchIndentsEvent {
    data class UpdateQuery(val query: String) : SearchIndentsEvent()
    data class UpdateFilters(
        val priority: String? = null,
        val clientId: String? = null,
        val assignedToMe: Boolean? = null
    ) : SearchIndentsEvent()
    object PerformSearch : SearchIndentsEvent()
    object ClearError : SearchIndentsEvent()
}
