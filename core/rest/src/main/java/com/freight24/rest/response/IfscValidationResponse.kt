package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class IfscValidationResponse(
    val success: Boolean = false,
    val data: IfscValidationData? = null,
)

@Serializable
data class IfscValidationData(
    val valid: Boolean = false,
    val ifsc: String = "",
    val bank: IfscBankData? = null,
)

@Serializable
data class IfscBankData(
    val name: String = "",
    val branch: String = "",
)
