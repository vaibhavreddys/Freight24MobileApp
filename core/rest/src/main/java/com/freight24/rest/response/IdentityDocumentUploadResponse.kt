package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IdentityDocumentUploadResponse(
    val data: IdentityDocumentUploadData? = null,
) : BaseResponse()

@Serializable
data class IdentityDocumentUploadData(
    @SerialName("temporaryFileKey")
    val temporaryFileKey: String? = null,
    @SerialName("fileKey")
    val fileKey: String? = null,
)
