package com.freight24.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class LrVerifyRequest(
    val status: String,
    val remarks: String?,
)
