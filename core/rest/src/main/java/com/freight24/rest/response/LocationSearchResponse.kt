package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationSearchResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: List<CityResult>,
)

@Serializable
data class CityResult(
    @SerialName("type") val type: String,
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("city") val city: String,
    @SerialName("state") val state: String,
)
