package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class UploadedDataResponse(
    val data: UploadedData,
) : BaseResponse() {
    @Serializable
    data class UploadedData(
        val document: DocumentResponse?,
        val trip: TripDataResponse?,
    )

    @Serializable
    data class DocumentResponse(
        val id: String,
        val type: String,
        val url: String,
        val status: String,
        val podType: String?,
        val podStatus: String?,
        val uploadedAt: String,
    )
}
