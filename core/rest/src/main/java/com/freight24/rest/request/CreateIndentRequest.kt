package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateIndentRequest(
    @SerialName("from_location_id") val fromLocationId: String,
    @SerialName("to_location_id") val toLocationId: String,
    @SerialName("client_id") val clientId: String,
    @SerialName("material_name") val materialName: String,
    @SerialName("weight") val weight: Double,
    @SerialName("truck_type_id") val truckTypeId: String,
    
    // Optional Fields
    @SerialName("client_cost") val clientCost: Double? = null,
    @SerialName("negotiated_cost") val negotiatedCost: Double? = null,
    @SerialName("business_proposed_cost") val businessProposedCost: Double? = null,
    @SerialName("vehicle_height") val vehicleHeight: Double? = null, // in feet
    @SerialName("flooring_type") val flooringType: String? = null, // "Wooden", "Steel"
    @SerialName("tat_required") val tatRequired: Boolean = false,
    @SerialName("tarpaulin_required") val tarpaulinRequired: Boolean = false,
    @SerialName("hide_cost") val hideCost: Boolean = false,
    @SerialName("remarks") val remarks: String? = null
)
