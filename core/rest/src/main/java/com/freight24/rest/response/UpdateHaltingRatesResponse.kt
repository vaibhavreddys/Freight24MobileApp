package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class UpdateHaltingRatesResponse(
    val data: UpdateHaltingRatesData ?= null
) : BaseResponse() {
    @Serializable
    data class UpdateHaltingRatesData(
        val loadingHaltingId: String?,
        val unloadingHaltingId: String?,
        val totalHaltingDays: Int?,
        val totalHaltingAmount: Double?
    )
}


