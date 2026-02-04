package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatedTripResponse(
    val data: TripData
) {
    @Serializable
    data class TripData(
        @SerialName("trip")  val trip: Trip,
        @SerialName("indent_closed") val indentClosed: Boolean,
    )

    @Serializable
    data class Trip(
        @SerialName("id") val id: String,
        @SerialName("indent_id") val indentId: String,
        @SerialName("truck_id") val truckId: String,
        @SerialName("status") val status: String,
        @SerialName("client_trip_cost") val clientTripCost: String,
        @SerialName("trucker_trip_cost") val truckerTripCost: String,
        @SerialName("advance_amount") val advanceAmount: String,
    )
}


