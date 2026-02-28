package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class UpdatedLUResponse(
    val data : Charges
) : BaseResponse()

@Serializable
data class Charges(
    val loadingChargeId: String? = null,
    val unloadingChargeId: String? = null
)
