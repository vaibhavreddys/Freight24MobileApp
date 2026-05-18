package com.freight24.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateAdhocTruckRequest(
    val phoneNumber: String,
    val truckOwnerId: String? = null,
    val truckOwnerName: String? = null,
    val truckTypeId: Int,
    val locationId: Int,
    val locationType: String,
)
