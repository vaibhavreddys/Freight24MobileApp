package com.freight24.common.repository

import com.freight24.common.model.Trip
import com.freight24.database.entity.TripEntity

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
