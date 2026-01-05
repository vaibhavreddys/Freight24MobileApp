package com.freight24.core.network.mapper

import com.freight24.core.common.model.Trip
import com.freight24.core.common.util.DateTimeUtils
import com.freight24.core.common.util.TruckNameFormatter
import com.freight24.core.network.model.TripDto
import com.freight24.core.network.model.TripLocationDto

fun TripDto.toDomain(): Trip = Trip(
    id = id,
    registrationNumber = registration_number,
    truckType = TruckNameFormatter.shortenTruckName(truck_type),
    status = trucker_status,
    createdAt = DateTimeUtils.formatRelativeTime(created_at), // Formatting will be done in UI or in a separate utility
    tripCost = trucker_trip_cost,
    advanceAmount = advance_amount,
    origin = origin.toDisplayString(),
    destination = destination.toDisplayString()
)

private fun TripLocationDto.toDisplayString(): String {
    return city ?: area ?: address ?: ""
}
