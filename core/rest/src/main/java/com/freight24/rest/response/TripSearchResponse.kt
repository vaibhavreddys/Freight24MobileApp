package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TripSearchResponse(
    val success: Boolean,
    val data: List<TripSearchResultDto>? = null,
)

@Serializable
data class TripSearchResultDto(
    val id: String,
    @SerialName("trip_code")
    val tripCode: String,
    @SerialName("registration_number")
    val registrationNumber: String,
    val origin: TripLocationDto? = null,
    val destination: TripLocationDto? = null,
)
