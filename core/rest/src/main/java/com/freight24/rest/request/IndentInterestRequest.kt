package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IndentInterestRequest(
    @SerialName("indent_id")
    val indentId: String,
)
