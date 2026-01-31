package com.freight24.rest.response


import kotlinx.serialization.Serializable

@Serializable
data class IndentResponse(
    val data : Indent
) : BaseResponse() {

    @Serializable
    data class Indent(
        val id: String,
        val assignedTo: String?,
        val assignedAt: String?,
        val status: String,
        val createdAt: String,
        val negotiatedTripCost: Double?,
        val hideCostInfo: Boolean,
        val indentGroupDetails: IndentGroupDetails
    )

    @Serializable
    data class IndentGroupDetails(
        val id: String,
        val status: String,
        val material: String,
        val priority: String,
        val clientId: String,
        val tatHours: Int?,
        val createdAt: String,
        val expiresAt: String,
        val loadWeight: Int,
        val tatEnabled: Boolean,
        val expiryHours: Int,
        val flooringType: String?,
        val hideCostInfo: Boolean,
        val vehicleHeight: Double?,
        val tarpaulingRequired: Boolean,
        val specialRequirements: String?,
        val clientTripCost: Double?,
        val businessProposedCost: Double?,
        val luIncludedInTripCost: Boolean?,
        val truckType: TruckType?,
        val clientProfile: ClientProfile?,
        val indentLocations: List<IndentLocation>
    )

    @Serializable
    data class TruckType(
        val bodyType: String,
        val truckType: String,
        val displayName: String
    )

    @Serializable
    data class ClientProfile(
        val id: String? = null,
        val fullName: String? = null,
        val companyName: String? = null,
        val phoneNumber: String? = null,
    )

    @Serializable
    data class IndentLocation(
        val id: String,
        val address: String?,
        val areaId: Int?,
        val areaName: String?,
        val cityId: Int?,
        val cityName: String?,
        val contactPhone: String?,
        val locationType: String,
        val contactPerson: String?,
        val sequenceOrder: Int
    )

}
