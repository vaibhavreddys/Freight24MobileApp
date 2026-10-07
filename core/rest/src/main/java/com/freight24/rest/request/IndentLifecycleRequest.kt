package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IndentLifecycleRequest(
    @SerialName("reasonCode") val reasonCode: String,
    @SerialName("remarks") val remarks: String? = null,
)
