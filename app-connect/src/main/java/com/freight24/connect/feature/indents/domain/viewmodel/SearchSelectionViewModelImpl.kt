package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.indents.domain.usecases.GetClientsUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetLocationsUseCase
import com.freight24.connect.feature.indents.views.SearchType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchSelectionViewModelImpl
    @Inject
    constructor(
        private val getLocationsUseCase: GetLocationsUseCase,
        private val getClientsUseCase: GetClientsUseCase,
    ) : ViewModel(), SearchSelectionViewModel {
        private val _uiState = MutableStateFlow(SearchSelectionUiState())
        override val uiState: StateFlow<SearchSelectionUiState> = _uiState.asStateFlow()

        private var searchJob: Job? = null

        override fun onEvent(event: SearchSelectionEvent) {
            when (event) {
                is SearchSelectionEvent.OnQueryChange -> {
                    _uiState.update { it.copy(query = event.query) }
                    search(event.query, event.searchType)
                }
            }
        }

        private fun search(
            query: String,
            searchType: SearchType,
        ) {
            searchJob?.cancel()
            if (query.length < 3) {
                _uiState.update { it.copy(results = emptyList(), isLoading = false) }
                return
            }

            searchJob =
                viewModelScope.launch {
                    delay(300)
                    _uiState.update { it.copy(isLoading = true, error = null) }

                    val result =
                        when (searchType) {
                            SearchType.CLIENT -> getClientsUseCase.invoke(query)
                            SearchType.LOCATION_FROM, SearchType.LOCATION_TO -> getLocationsUseCase.invoke(query)
                        }

                    if (result.isSuccess) {
                        _uiState.update { it.copy(results = result.getOrNull() ?: emptyList(), isLoading = false) }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exceptionOrNull()?.message ?: "Search failed",
                            )
                        }
                    }
                }
        }
    }
