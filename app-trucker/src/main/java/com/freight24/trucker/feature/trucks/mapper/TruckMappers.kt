package com.freight24.trucker.feature.trucks.mapper

import com.freight24.common.model.Truck
import com.freight24.rest.response.TruckDto

fun TruckDto.toDomain(): Truck {
    return Truck(
        id = truckId,
        registrationNumber = registrationNumber,
        isVerified = isVerified,
        isBlacklisted = isBlacklisted,
        availability = availability,
        truckType = truckType,
        bodyType = bodyType
    )
}
