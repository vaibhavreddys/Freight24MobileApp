package com.freight24.connect.feature.dashboard.domain.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.usecase.EmptyUseCaseParams
import com.freight24.connect.feature.home.usecase.DashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModelImpl @Inject constructor(
    private val dashboardUseCase: DashboardUseCase
) : ViewModel(), DashboardViewModel {

    override var uiState by mutableStateOf(DashboardUiState())
        private set

    init {
        loadData()
    }

    override fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.Retry,
            is DashboardEvent.Refresh -> loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            dashboardUseCase(EmptyUseCaseParams).fold(
                onSuccess = { data ->
                    uiState = uiState.copy(
                        isLoading = false,
                        data = data
                    )
                },
                onFailure = { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        error = error.localizedMessage ?: "Failed to load dashboard data"
                    )
                }
            )
        }
    }
}
