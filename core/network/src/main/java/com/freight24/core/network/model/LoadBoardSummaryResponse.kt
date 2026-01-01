package com.freight24.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class LoadBoardSummaryResponse(
    val success: Boolean,
    val data: List<CityLoadSummaryDto>?
)
