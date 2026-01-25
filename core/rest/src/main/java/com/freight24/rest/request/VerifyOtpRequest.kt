
package com.freight24.rest.request

data class VerifyOtpRequest(
    val phone_number: String,
    val otp: String,
    val role_id: Int?= null,
)
