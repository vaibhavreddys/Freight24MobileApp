package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateVerificationRequestStatusRequest(
    @SerialName("status") val status: String,
    @SerialName("resolution_notes") val resolutionNotes: String? = null,
)
