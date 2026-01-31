package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.indents.domain.usecases.GetMyIndentsUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetMyIndentsUseCaseParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IndentsListViewModelImpl @Inject constructor(
    private val getMyIndentsUseCase: GetMyIndentsUseCase
) : ViewModel(), IndentsListViewModel {

    private val _uiState = MutableStateFlow(IndentsListUiState())
    override val uiState: StateFlow<IndentsListUiState> = _uiState.asStateFlow()

    init {
        loadIndents()
    }

    override fun onEvent(event: IndentsListEvent) {
        when (event) {
            is IndentsListEvent.SelectTab -> {
                _uiState.update { it.copy(selectedTab = event.tab) }
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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = getMyIndentsUseCase.invoke(
                GetMyIndentsUseCaseParams(
                    status = "OPEN",
                    page = 1,
                    limit = 50
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
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to load indents"
                    )
                }
            }
        }
    }
}
