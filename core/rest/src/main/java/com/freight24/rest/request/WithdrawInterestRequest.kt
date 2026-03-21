package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WithdrawInterestRequest(
    @SerialName("interest_id") val interestId: String,
    @SerialName("remarks") val remarks: String
)
