package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class SearchTruckResponse(
    val data: List<Truck>,
) : BaseResponse() {
    @Serializable
    data class Truck(
        val truckId: String,
        val registrationNumber: String,
        val truckType: String,
        val displayName: String,
        val make: String,
        val model: String,
        val status: String,
        val isBlacklisted: Boolean,
        val owner: TruckOwner,
        val availability: String,
        val activeTrip: ActiveTrip?,
    )

    @Serializable
    data class TruckOwner(
        val name: String,
        val phone: String,
        val tagCode: String,
    )

    @Serializable
    data class ActiveTrip(
        val tripId: String,
        val truckerStatus: String,
    )
}
