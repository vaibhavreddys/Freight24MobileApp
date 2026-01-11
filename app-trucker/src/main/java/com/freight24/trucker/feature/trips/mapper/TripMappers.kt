package com.freight24.trucker.feature.trips.mapper

import com.freight24.common.util.DateTimeUtils
import com.freight24.common.util.TruckNameFormatter
import com.freight24.database.entity.TripEntity
import com.freight24.rest.response.TripDto
import com.freight24.rest.response.TripLocationDto
import com.freight24.trucker.feature.trips.data.model.Trip

fun Trip.toEntity(page: Int): TripEntity =
    TripEntity(
        id = id,
        registrationNumber = registrationNumber,
        truckType = truckType,
        status = status,
        createdAt = createdAt,
        tripCost = tripCost,
        advanceAmount = advanceAmount,
        origin = origin,
        destination = destination,
        page = page,
    )

fun TripEntity.toDomain(): Trip =
    Trip(
        id = id,
        registrationNumber = registrationNumber,
        truckType = truckType,
        status = status,
        createdAt = createdAt,
        tripCost = tripCost,
        advanceAmount = advanceAmount,
        origin = origin,
        destination = destination,
    )

fun TripDto.toDomain(): Trip =
    Trip(
        id = id,
        registrationNumber = registration_number,
        truckType = TruckNameFormatter.shortenTruckName(truck_type),
        status = trucker_status,
        createdAt = DateTimeUtils.formatRelativeTime(created_at), // Formatting will be done in UI or in a separate utility
        tripCost = trucker_trip_cost,
        advanceAmount = advance_amount,
        origin = origin.toDisplayString(),
        destination = destination.toDisplayString(),
    )

private fun TripLocationDto.toDisplayString(): String {
    return city ?: area ?: address ?: ""
}