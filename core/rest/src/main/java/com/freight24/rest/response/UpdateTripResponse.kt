package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTripResponse(
    val data: TripDataResponse
) : BaseResponse()
