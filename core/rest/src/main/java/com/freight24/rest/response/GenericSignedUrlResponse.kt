package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class GenericSignedUrlResponse(
    val data: GenericSignedDocument
) : BaseResponse()

@Serializable
data class GenericSignedDocument(
    val originalUrl: String,
    val signedUrl: String,
    val expiresIn: Int
)
