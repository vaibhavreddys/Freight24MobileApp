package com.freight24.connect.domain.model

data class NamedLocation(
    val id: Int?,
    val name: String?,
)

data class ActivationEligibility(
    val eligible: Boolean,
    val missingRequirements: List<MissingRequirement>,
)

data class MissingRequirement(
    val field: String?,
    val code: String?,
    val label: String?,
    val message: String?,
)

data class VerificationDocument(
    val documentId: String?,
    val type: String,
    val status: String,
    val url: String?,
    val verifiedBy: String? = null,
    val verifiedAt: String? = null,
    val rejectionReason: String? = null,
)
