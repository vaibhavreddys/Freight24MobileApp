package com.freight24.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class CloneIndentRequest(
    val assigned_to: String?,
    val notes: String?
)
