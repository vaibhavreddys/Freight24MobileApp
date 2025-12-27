package com.freight24.core.network.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpRequest(
    val phone_number: String,
    val otp: String,
    @EncodeDefault
    val role_id: Int = 2  // Trucker role
)