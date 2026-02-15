package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class MyTripsDataResponse(
    val page: Int,
    val limit: Int,
    val data: List<TripDataResponse>
) : BaseResponse()