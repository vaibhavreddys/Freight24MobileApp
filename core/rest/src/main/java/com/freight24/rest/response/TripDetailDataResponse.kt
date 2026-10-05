package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class TripDetailDataResponse(
    val data: TripDataResponse,
) : BaseResponse()
