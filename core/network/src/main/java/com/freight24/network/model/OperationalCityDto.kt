package com.freight24.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OperationalCityDto(
    @SerialName("operational_city")
    val operationalCity: String,
    val loads: List<LoadDto> = emptyList(),
)
