package com.freight24.connect.feature.dashboard.domain.viewmodel

import com.freight24.rest.response.DashboardDataResponse

interface DashboardViewModel {
    val uiState: DashboardUiState

    fun onEvent(event: DashboardEvent)
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val data: DashboardDataResponse.DashboardData? = null,
    val error: String? = null,
)

sealed class DashboardEvent {
    object Retry : DashboardEvent()

    object Refresh : DashboardEvent()
}
