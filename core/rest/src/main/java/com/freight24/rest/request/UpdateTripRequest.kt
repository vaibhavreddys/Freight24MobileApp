package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateTripRequest(
    @SerialName("driver_name") val driverName: String?,
    @SerialName("driver_phone") val driverPhone: String?,
    @SerialName("client_trip_cost") val clientTripCost: String?,
    @SerialName("trucker_trip_cost") val truckerTripCost: String?,
    @SerialName("lu_included") val luIncluded: Boolean?,
    @SerialName("truck_id") val truckId: String?,
    @SerialName("reason") val reason: String?,
)
