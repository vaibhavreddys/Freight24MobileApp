package com.freight24.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateOtherDeductionsRequest(
    val amount: String,
    val remarks: String?,
)
