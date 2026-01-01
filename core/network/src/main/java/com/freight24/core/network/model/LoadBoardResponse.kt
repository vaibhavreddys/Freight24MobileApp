package com.freight24.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class LoadBoardResponse(
    val success: Boolean,
    val data: List<OperationalCityDto>? = null
)

