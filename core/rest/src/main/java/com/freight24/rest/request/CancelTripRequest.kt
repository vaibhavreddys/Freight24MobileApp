package com.freight24.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class CancelTripRequest(
    val reason: String
)
