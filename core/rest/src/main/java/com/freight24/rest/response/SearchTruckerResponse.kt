package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class SearchTruckerResponse(
    val data: TruckerData,
) : BaseResponse() {
    @Serializable
    data class TruckerData(
        val truckers: List<Trucker>,
        val totalOwners: Int,
    )

    @Serializable
    data class Trucker(
        val id: String,
        val name: String,
        val phone: String,
        val score: String,
        val trucks: List<Truck>,
    )

    @Serializable
    data class Truck(
        val score: String,
        val truckId: String,
        val matchReasons: List<String>,
        val registrationNumber: String,
    )
}
