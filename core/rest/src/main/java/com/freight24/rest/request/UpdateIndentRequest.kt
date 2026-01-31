package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateIndentRequest(
    @SerialName("status") val status: String? = null,
    @SerialName("negotiated_trip_cost") val negotiatedTripCost: Int? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("hide_cost_info") val hideCostInfo: Boolean? = null,
    @SerialName("lu_included_in_trip_cost") val luIncludedInTripCost: Boolean? = null,
    @SerialName("client_trip_cost") val clientTripCost: Int? = null,
    @SerialName("business_proposed_cost") val businessProposedCost: Int? = null,
    @SerialName("vehicle_height") val vehicleHeight: Float? = null,
    @SerialName("flooring_type") val flooringType: String? = null,
    @SerialName("tarpauling_required") val tarpaulingRequired: Boolean? = null,
    @SerialName("tat_enabled") val tatEnabled: Boolean? = null,
    @SerialName("tat_hours") val tatHours: Int? = null,
    @SerialName("special_requirements") val remarks: String?= null
)
