package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TruckerIndentDetailResponse(
    val data: TruckerIndentDetail? = null,
) : BaseResponse()

@Serializable
data class TruckerIndentDetail(
    val id: String,
    val indentCode: String? = null,
    val status: String,
    val createdAt: String,
    val hideCostInfo: Boolean = false,
    val negotiatedTripCost: Double? = null,
    val fleetExecutive: FleetExecutive? = null,
    val indentGroupDetails: TruckerIndentGroupDetails,
    val metrics: TruckerMetrics? = null,
)

@Serializable
data class TruckerIndentGroupDetails(
    val id: String,
    val status: String,
    val material: String,
    val priority: String,
    val clientId: String,
    val tatHours: Int? = null,
    val createdAt: String,
    val expiresAt: String,
    val loadWeight: Int,
    val tatEnabled: Boolean,
    val expiryHours: Int,
    val flooringType: String? = null,
    val vehicleHeight: Double? = null,
    val tarpaulingRequired: Boolean = false,
    val specialRequirements: String? = null,
    val clientTripCost: Double? = null,
    val businessProposedCost: Double? = null,
    val luIncludedInTripCost: Boolean? = null,
    val truckType: TruckerTruckType? = null,
    val clientProfile: TruckerClientProfile? = null,
    val indentLocations: List<TruckerIndentLocation> = emptyList(),
)

@Serializable
data class TruckerTruckType(
    val id: String? = null,
    val bodyType: String,
    val truckType: String,
    val displayName: String,
)

@Serializable
data class TruckerClientProfile(
    val id: String? = null,
    val companyName: String? = null,
    val tagCode: String? = null,
)

@Serializable
data class TruckerIndentLocation(
    val id: String,
    val address: String? = null,
    val areaId: Int? = null,
    val areaName: String? = null,
    val cityId: Int? = null,
    val cityName: String? = null,
    val contactPhone: String? = null,
    val locationType: String,
    val contactPerson: String? = null,
    val sequenceOrder: Int = 0,
)

@Serializable
data class TruckerFleetExecutive(
    val id: String,
    val name: String? = null,
    val phone: String? = null,
)

typealias FleetExecutive = TruckerFleetExecutive

@Serializable
data class TruckerMetrics(
    val interestCount: Int = 0,
)
