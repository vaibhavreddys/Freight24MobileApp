package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class SignedDocument(
    val data: Document,
) : BaseResponse() {
    @Serializable
    data class Document(
        val documentId: String? = null,
        val signedUrl: String,
        val expiresIn: Int,
    )
}
