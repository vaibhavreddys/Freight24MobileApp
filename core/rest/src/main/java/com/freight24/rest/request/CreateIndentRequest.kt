package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateIndentRequest(
    @SerialName("client_id") val clientId: String,
    @SerialName("truck_type_id") val truckTypeId: Int,
    @SerialName("load_weight") val loadWeight: Int,
    @SerialName("material") val material: String,
    @SerialName("client_trip_cost") val clientTripCost: Int? = null,
    @SerialName("negotiated_trip_cost") val negotiatedTripCost: Int? = null,
    @SerialName("business_proposed_cost") val businessProposedCost: Int? = null,
    @SerialName("vehicle_height") val vehicleHeight: Float? = null,
    @SerialName("hide_cost_info") val hideCostInfo: Boolean = true,
    @SerialName("tat_enabled") val tatEnabled: Boolean = false,
    @SerialName("tat_hours") val tatHours: Int? = null,
    @SerialName("tarpauling_required") val tarpaulingRequired: Boolean = false,
    @SerialName("special_requirements") val remarks: String? = null,
    @SerialName("flooring_type") val flooringType: String = "Steel",
    @SerialName("locations") val locations: List<Location>,
) {

    @Serializable
    data class Location(
        @SerialName("location_type")  val locationType: String,
        @SerialName("area_id") val areaId: Int?= null,
        @SerialName("city_id") val cityId: Int? = null,
        @SerialName("address") val address: String? = null,
        @SerialName("contact_person") val contactPerson: String? = null,
        @SerialName("contact_phone") val contactPhone: String? = null,
        @SerialName("sequence_order") val sequenceOrder: Int
    )
}


