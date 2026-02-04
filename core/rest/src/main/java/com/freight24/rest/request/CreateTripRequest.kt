package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateTripRequest (
    @SerialName("indent_id") val indentId: String,
    @SerialName("truck_id") val truckId: String,
    @SerialName("driver_name") val driverName: String?,
    @SerialName("driver_phone") val driverPhone: String?,
)
