package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IndentInterestResponse(
    val data: IndentInterestData? = null,
) : BaseResponse()

@Serializable
data class IndentInterestData(
    @SerialName("interest_id")
    val interestId: String,
    @SerialName("indent_id")
    val indentId: String,
    val status: String,
    @SerialName("is_assigned")
    val isAssigned: Boolean,
)
