package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PreferredRouteEntry(
    @SerialName("source_city_id")
    val sourceCityId: Int,
    @SerialName("destination_city_id")
    val destinationCityId: Int,
    val priority: Int,
)
