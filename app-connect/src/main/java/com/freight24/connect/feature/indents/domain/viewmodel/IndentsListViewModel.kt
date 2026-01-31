package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.compose.runtime.Immutable
import com.freight24.rest.response.IndentResponse
import kotlinx.coroutines.flow.StateFlow

interface IndentsListViewModel {
    val uiState: StateFlow<IndentsListUiState>
    fun onEvent(event: IndentsListEvent)
}

@Immutable
data class IndentsListUiState(
    val isLoading: Boolean = false,
    val indents: List<IndentResponse.Indent> = emptyList(),
    val selectedTab: IndentTab = IndentTab.MINE,
    val errorMessage: String? = null
)

enum class IndentTab {
    MINE,
    ALL
}

sealed class IndentsListEvent {
    data class SelectTab(val tab: IndentTab) : IndentsListEvent()
    object RefreshIndents : IndentsListEvent()
    object ClearError : IndentsListEvent()
}
