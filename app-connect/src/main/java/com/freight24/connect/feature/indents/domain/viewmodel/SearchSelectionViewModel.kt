package com.freight24.connect.feature.indents.domain.viewmodel

import com.freight24.connect.feature.indents.views.SearchType
import kotlinx.coroutines.flow.StateFlow

data class SearchSelectionUiState(
    val query: String = "",
    val results: List<Any> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class SearchSelectionEvent {
    data class OnQueryChange(val query: String, val searchType: SearchType) : SearchSelectionEvent()
}

interface SearchSelectionViewModel {
    val uiState: StateFlow<SearchSelectionUiState>
    fun onEvent(event: SearchSelectionEvent)
}
