package com.freight24.core.common.model

data class CityPaginationState(
    val page: Int = 1,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false
)
