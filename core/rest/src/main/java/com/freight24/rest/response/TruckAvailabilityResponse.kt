package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class TruckAvailabilityResponse(
    val data: String? = null,
) : BaseResponse()
