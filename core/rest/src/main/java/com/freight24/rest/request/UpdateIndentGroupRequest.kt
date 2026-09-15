package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Group-level indent update — `PUT /api/v1/indents/groups/:groupId`.
 *
 * Distinct from [UpdateIndentRequest], which targets a single child indent
 * (`PUT /api/v1/indents/:id`) and cannot touch the contact roles. The backend
 * accepts this one only while the group is still `OPEN`, and only from the
 * group's creator or an admin / operations manager.
 *
 * Every field is nullable so the caller can send a partial body; the backend
 * falls back to the existing value for anything omitted.
 */
@Serializable
data class UpdateIndentGroupRequest(
    @SerialName("expiry_hours") val expiryHours: Int? = null,
    @SerialName("priority") val priority: String? = null,
    @SerialName("business_proposed_cost") val businessProposedCost: Double? = null,
    @SerialName("client_trip_cost") val clientTripCost: Double? = null,
    @SerialName("hide_cost_info") val hideCostInfo: Boolean? = null,
    @SerialName("special_requirements") val remarks: String? = null,
    @SerialName("contacts") val contacts: IndentContactsRequest? = null,
)
