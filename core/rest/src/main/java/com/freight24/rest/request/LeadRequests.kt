package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LeadRouteEntryRequest(
    @SerialName("source_city_id") val sourceCityId: Int,
    @SerialName("destination_city_id") val destinationCityId: Int,
    val priority: Int = 1,
)

@Serializable
data class UpdateLeadDetailsRequest(
    val name: String? = null,
    @SerialName("base_city_id") val baseCityId: Int? = null,
    @SerialName("truck_type_ids") val truckTypeIds: List<Int>? = null,
    val routes: List<LeadRouteEntryRequest>? = null,
    val notes: String? = null,
)

@Serializable
data class CallOutcomeRequest(
    val outcome: String,
    val notes: String? = null,
    @SerialName("not_interested_reason") val notInterestedReason: String? = null,
    @SerialName("invalid_reason") val invalidReason: String? = null,
)

@Serializable
data class ConvertLeadRequest(
    val name: String? = null,
    @SerialName("base_city_id") val baseCityId: Int? = null,
    val email: String? = null,
    @SerialName("business_name") val businessName: String? = null,
)
