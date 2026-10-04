package com.freight24.rest.response

import kotlinx.serialization.Serializable

/**
 * Response of the POD upload/replace endpoints — the backend returns the
 * stored `trip_documents` row (snake_case). The repository re-fetches the
 * full trip right after a successful upload, so this only has to parse.
 */
@Serializable
data class PodUploadResponse(
    val success: Boolean,
    val data: TripDocumentDto? = null,
    val message: String? = null,
)
