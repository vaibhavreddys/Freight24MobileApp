package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class TripResponse(
    val success: Boolean,
    val data: List<TripDto>?,
    val pagination: TripPaginationDto?,
)

@Serializable
data class TripDto(
    val id: String,
    val registration_number: String,
    val truck_type: String,
    val trucker_status: String,
    val created_at: String,
    val trucker_trip_cost: Double,
    val advance_amount: Double,
    val origin: TripLocationDto,
    val destination: TripLocationDto,
)

@Serializable
data class TripLocationDto(
    val city: String?,
    val area: String?,
    val address: String?,
)

@Serializable
data class TripPaginationDto(
    val page: Int,
    val limit: Int,
    val total: Int?,
    val pages: Int,
)
