package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.indents.domain.usecases.SearchIndentsUseCase
import com.freight24.connect.feature.indents.domain.usecases.SearchIndentsUseCaseParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchIndentsViewModelImpl @Inject constructor(
    private val searchIndentsUseCase: SearchIndentsUseCase
) : ViewModel(), SearchIndentsViewModel {

    private val _uiState = MutableStateFlow(SearchIndentsUiState())
    override val uiState: StateFlow<SearchIndentsUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private val queryFlow = MutableStateFlow("")

    init {
        observeQuery()
    }

    @OptIn(FlowPreview::class)
    private fun observeQuery() {
        queryFlow
            .debounce(500) // Debounce for 500ms
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isNotBlank()) {
                    performSearch(query)
                } else {
                    _uiState.update { it.copy(indents = emptyList()) }
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: SearchIndentsEvent) {
        when (event) {
            is SearchIndentsEvent.UpdateQuery -> {
                _uiState.update { it.copy(query = event.query) }
                queryFlow.value = event.query
            }
            is SearchIndentsEvent.UpdateFilters -> {
                _uiState.update {
                    it.copy(
                        priority = event.priority ?: it.priority,
                        clientId = event.clientId ?: it.clientId,
                        assignedToMe = event.assignedToMe ?: it.assignedToMe
                    )
                }
                // Trigger search with new filters if query is present
                if (_uiState.value.query.isNotBlank()) {
                    performSearch(_uiState.value.query)
                }
            }
            is SearchIndentsEvent.PerformSearch -> {
                performSearch(_uiState.value.query)
            }
            is SearchIndentsEvent.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val currentState = _uiState.value
            val result = searchIndentsUseCase(
                SearchIndentsUseCaseParams(
                    query = query,
                    priority = currentState.priority,
                    clientId = currentState.clientId,
                    assignedToMe = if (currentState.assignedToMe) true else null
                )
            )

            if (result.isSuccess) {
                val response = result.getOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        indents = response?.data ?: emptyList()
                    )
                }
            } else {
                 _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Search failed"
                    )
                }
            }
        }
    }
}
