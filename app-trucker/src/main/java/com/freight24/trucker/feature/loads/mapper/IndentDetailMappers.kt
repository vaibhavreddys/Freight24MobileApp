package com.freight24.trucker.feature.loads.mapper

import com.freight24.common.util.formatLoadWeight
import com.freight24.rest.response.TruckerIndentDetail
import com.freight24.rest.response.TruckerIndentDetailResponse
import com.freight24.ui.components.LoadDetail
import com.freight24.ui.components.LoadDetailClient
import com.freight24.ui.components.LoadDetailF24Agent
import com.freight24.ui.components.LoadDetailLocation
import com.freight24.ui.components.LoadDetailTruckType

fun TruckerIndentDetailResponse.toLoadDetailOrNull(
    tripPrice: String,
    isInterested: Boolean,
    isInterestLoading: Boolean,
): LoadDetail? = data?.toLoadDetail(
    tripPrice = tripPrice,
    isInterested = isInterested,
    isInterestLoading = isInterestLoading,
)

fun TruckerIndentDetail.toLoadDetail(
    tripPrice: String,
    isInterested: Boolean,
    isInterestLoading: Boolean,
): LoadDetail {
    val group = indentGroupDetails
    val loading = group.indentLocations
        .filter { it.locationType.equals("loading", ignoreCase = true) }
        .maxByOrNull { it.sequenceOrder }
    val unloading = group.indentLocations
        .filter { it.locationType.equals("unloading", ignoreCase = true) }
        .maxByOrNull { it.sequenceOrder }

    return LoadDetail(
        indentCode = indentCode?.takeIf { it.isNotBlank() } ?: "Indent ${id.take(6)}",
        priority = group.priority,
        createdAt = createdAt,
        tripPrice = tripPrice,
        hideCostInfo = hideCostInfo,
        loadWeight = formatLoadWeight(group.loadWeight, "Tons"),
        material = group.material,
        commodity = group.material,
        truckType = group.truckType?.let {
            LoadDetailTruckType(
                bodyType = it.bodyType,
                truckType = it.truckType,
                displayName = it.displayName,
            )
        },
        flooringType = group.flooringType,
        vehicleHeight = group.vehicleHeight?.let { "%.2f ft".format(it) },
        tarpaulingRequired = group.tarpaulingRequired,
        tatEnabled = group.tatEnabled,
        tatHours = group.tatHours,
        luIncludedInTripCost = group.luIncludedInTripCost,
        specialRequirements = group.specialRequirements,
        expiresAt = group.expiresAt,
        client = group.clientProfile?.let {
            LoadDetailClient(
                companyName = it.companyName,
                tagCode = it.tagCode,
            )
        },
        loadingLocation = loading?.toLocation(),
        unloadingLocation = unloading?.toLocation(),
        interestCount = metrics?.interestCount ?: 0,
        agent = fleetExecutive?.let { exec ->
            LoadDetailF24Agent(
                name = exec.name.orEmpty(),
                phone = exec.phone.orEmpty(),
            )
        },
        isInterested = isInterested,
        isInterestLoading = isInterestLoading,
    )
}

private fun com.freight24.rest.response.TruckerIndentLocation.toLocation(): LoadDetailLocation =
    LoadDetailLocation(
        address = address.orEmpty(),
        areaName = areaName,
        cityName = cityName,
        contactPerson = contactPerson,
        contactPhone = contactPhone,
        sequenceOrder = sequenceOrder,
    )
