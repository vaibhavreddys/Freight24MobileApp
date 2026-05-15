package com.freight24.connect.feature.dashboard.domain.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.usecase.EmptyUseCaseParams
import com.freight24.connect.feature.dashboard.domain.usecase.DashboardUseCase
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
        loadData(isRefresh = false)
    }

    override fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.Retry -> loadData(isRefresh = false)
            is DashboardEvent.Refresh -> loadData(isRefresh = true)
        }
    }

    private fun loadData(isRefresh: Boolean) {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                error = null
            )
            dashboardUseCase(EmptyUseCaseParams).fold(
                onSuccess = { data ->
                    uiState = uiState.copy(
                        isLoading = false,
                        isRefreshing = false,
                        data = data
                    )
                },
                onFailure = { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = error.localizedMessage ?: "Failed to load dashboard data"
                    )
                }
            )
        }
    }
}
