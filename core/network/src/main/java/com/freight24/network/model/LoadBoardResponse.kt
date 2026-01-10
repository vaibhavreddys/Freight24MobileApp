package com.freight24.network.model

import kotlinx.serialization.Serializable

@Serializable
data class LoadBoardResponse(
    val success: Boolean,
    val data: List<OperationalCityDto>? = null,
)
