package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class TruckHaltingRateResponse(
    val data: TruckHaltingRate? = null,
) : BaseResponse()

@Serializable
data class TruckHaltingRate(
    val truckTypeId: String,
    val haltingRatePerDay: String,
    val effectiveFrom: String?,
    val effectiveTo: String?,
)
