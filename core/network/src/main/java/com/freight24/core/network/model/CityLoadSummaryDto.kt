package com.freight24.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CityLoadSummaryDto(
    @SerialName("operational_city")
    val operationalCity: String,

    @SerialName("operational_city_id")
    val operationalCityId: Int,

    @SerialName("total_loads")
    val totalLoads: Int,

    @SerialName("latest_loads")
    val latestLoads: List<LoadDto>
)