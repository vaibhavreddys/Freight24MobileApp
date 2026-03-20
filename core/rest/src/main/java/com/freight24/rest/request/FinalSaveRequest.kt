package com.freight24.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class FinalSaveFileRequest(
    val temporaryKey: String,
    val document_type_id: Int
)

@Serializable
data class FinalSaveRequest(
    val user_id: String?,
    val files: List<FinalSaveFileRequest>
)
