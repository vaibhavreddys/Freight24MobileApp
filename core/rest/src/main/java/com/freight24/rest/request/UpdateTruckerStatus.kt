package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateTruckerStatus(
    @SerialName("trucker_status") val truckerStatus: String,
    val remarks: String?,
)
