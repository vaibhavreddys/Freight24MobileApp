package com.freight24.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class RequestOtpRequest(
    val phone_number: String
)