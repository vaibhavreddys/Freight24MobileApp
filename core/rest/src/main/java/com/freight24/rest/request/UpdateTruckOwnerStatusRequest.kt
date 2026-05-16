package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateTruckOwnerStatusRequest(
    @SerialName("status") val status: String
)
