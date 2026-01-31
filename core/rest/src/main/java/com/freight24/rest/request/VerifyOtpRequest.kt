
package com.freight24.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpRequest(
    val phone_number: String,
    val otp: String,
    val role_id: Int? = null,
)
