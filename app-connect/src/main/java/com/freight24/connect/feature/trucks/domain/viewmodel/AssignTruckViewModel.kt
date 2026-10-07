package com.freight24.connect.feature.trucks.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.trucks.domain.usecases.SearchTruckUseCase
import com.freight24.connect.feature.trucks.domain.usecases.SearchTrucksUseCaseParams
import com.freight24.connect.feature.trucks.enums.TruckStatus
import com.freight24.connect.domain.model.TruckAssignmentOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssignTruckUiState(
    val query: String = "",
    val trucks: List<TruckAssignmentOption> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val recentSearches: List<String> = emptyList(), // Placeholder for now
)

sealed class AssignTruckEvent {
    data class UpdateQuery(val query: String) : AssignTruckEvent()

    object Search : AssignTruckEvent()

    object ClearSearch : AssignTruckEvent()
}

@HiltViewModel
class AssignTruckViewModel
    @Inject
    constructor(
        private val searchTruckUseCase: SearchTruckUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AssignTruckUiState())
        val uiState: StateFlow<AssignTruckUiState> = _uiState.asStateFlow()

        private var searchJob: Job? = null

        fun onEvent(event: AssignTruckEvent) {
            when (event) {
                is AssignTruckEvent.UpdateQuery -> {
                    _uiState.update { it.copy(query = event.query) }
                    // Debounce search
                    searchJob?.cancel()
                    if (event.query.length >= 3) {
                        searchJob =
                            viewModelScope.launch {
                                delay(500) // 500ms debounce
                                performSearch(event.query)
                            }
                    } else {
                        _uiState.update { it.copy(trucks = emptyList(), errorMessage = null) }
                    }
                }
                is AssignTruckEvent.Search -> {
                    searchJob?.cancel()
                    if (_uiState.value.query.length >= 3) {
                        performSearch(_uiState.value.query)
                    }
                }
                is AssignTruckEvent.ClearSearch -> {
                    _uiState.update { it.copy(query = "", trucks = emptyList(), errorMessage = null) }
                }
            }
        }

        private fun performSearch(query: String) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val result = searchTruckUseCase(SearchTrucksUseCaseParams(query = query))

                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            trucks = result.getOrThrow().items.sortedByDescending { TruckStatus.from(it).isAssignable },
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Search failed",
                        )
                    }
                }
            }
        }
    }
