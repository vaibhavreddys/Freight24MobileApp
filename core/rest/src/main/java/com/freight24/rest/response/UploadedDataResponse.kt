package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class UploadedDataResponse(
    val document: DocumentResponse?,
    val trip: TripDataResponse?
): BaseResponse()

@Serializable
data class DocumentResponse(
    val id: String,
    val type: String,
    val url: String,
    val status: String,
    val podType: String?,
    val podStatus: String?,
    val uploadedAt: String
)
