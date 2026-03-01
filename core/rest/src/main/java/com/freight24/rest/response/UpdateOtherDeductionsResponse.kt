package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class UpdateOtherDeductionsResponse(
    val data: OtherDeductions
) : BaseResponse() {

    @Serializable
    data class OtherDeductions(
        val tripId: String?,
        val oldAmount: String?,
        val newAmount: String?,
    )

}