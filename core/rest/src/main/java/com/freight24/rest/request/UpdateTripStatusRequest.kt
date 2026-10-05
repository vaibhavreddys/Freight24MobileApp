package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateTripStatusRequest(
    @SerialName("trucker_status")
    val trucker_status: String,
)
