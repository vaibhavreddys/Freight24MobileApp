package com.freight24.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class SignedUrlRequest(
    val s3Url: String
)
