package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.indents.domain.usecases.GetAllIndentsUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetAllIndentsUseCaseParams
import com.freight24.connect.feature.indents.domain.usecases.GetMyIndentsUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetMyIndentsUseCaseParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IndentsListViewModelImpl
    @Inject
    constructor(
        private val getMyIndentsUseCase: GetMyIndentsUseCase,
        private val getAllIndentsUseCase: GetAllIndentsUseCase,
    ) : ViewModel(), IndentsListViewModel {
        private val _uiState = MutableStateFlow(IndentsListUiState())
        override val uiState: StateFlow<IndentsListUiState> = _uiState.asStateFlow()
        private var loadJob: Job? = null

        init {
            loadIndents()
        }

        override fun onEvent(event: IndentsListEvent) {
            when (event) {
                is IndentsListEvent.SelectTab -> {
                    _uiState.update { it.copy(selectedTab = event.tab) }
                    loadIndents()
                }
                is IndentsListEvent.SelectStatusFilter -> {
                    if (_uiState.value.statusFilter == event.filter) return
                    _uiState.update {
                        it.copy(
                            statusFilter = event.filter,
                            selectedTab =
                                if (
                                    event.filter == IndentStatusFilter.INACTIVE &&
                                    it.selectedTab == IndentTab.READY_FOR_ASSIGNMENT
                                ) {
                                    IndentTab.MINE
                                } else {
                                    it.selectedTab
                                },
                        )
                    }
                    loadIndents()
                }
                is IndentsListEvent.RefreshIndents -> {
                    loadIndents()
                }
                is IndentsListEvent.ClearError -> {
                    _uiState.update { it.copy(errorMessage = null) }
                }
            }
        }

        private fun loadIndents() {
            loadJob?.cancel()
            loadJob = viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val result =
                    when (_uiState.value.selectedTab) {
                        IndentTab.MINE ->
                            getMyIndentsUseCase.invoke(
                                GetMyIndentsUseCaseParams(
                                    status = _uiState.value.statusFilter.name,
                                    page = 1,
                                    limit = 50,
                                ),
                            )
                        IndentTab.ALL ->
                            getAllIndentsUseCase.invoke(
                                GetAllIndentsUseCaseParams(
                                    page = 1,
                                    limit = 50,
                                    inactive = _uiState.value.statusFilter == IndentStatusFilter.INACTIVE,
                                ),
                            )
                        IndentTab.READY_FOR_ASSIGNMENT ->
                            getMyIndentsUseCase.invoke(
                                GetMyIndentsUseCaseParams(
                                    status = "READY_FOR_ASSIGNMENT",
                                    page = 1,
                                    limit = 50,
                                ),
                            )
                    }

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            indents = response?.data ?: emptyList(),
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to load indents",
                        )
                    }
                }
            }
        }
    }
