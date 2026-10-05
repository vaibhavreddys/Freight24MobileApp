package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatedTripResponse(
    val data: TripData,
) {
    @Serializable
    data class TripData(
        @SerialName("trip") val trip: Trip,
        @SerialName("indent_closed") val indentClosed: Boolean,
    )

    @Serializable
    data class Trip(
        val id: String,
        val indentId: String,
        val tripCode: String,
        val status: String,
        val clientTripCost: String,
        val truckerTripCost: String,
        val tripLocations: List<Location>,
    )

    @Serializable
    data class Location(
        val id: String,
        val address: String?,
        val areaId: String?,
        val areaName: String?,
        val cityId: String?,
        val cityName: String?,
        val locationType: String,
        val sequenceOrder: Int?,
    )
}
