package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshSessionRequest(
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("installation_id")
    val installationId: String,
)
