package com.freight24.trucker.feature.trucks.mapper

import com.freight24.common.model.Truck
import com.freight24.common.model.TruckDetail
import com.freight24.common.model.TruckDocument
import com.freight24.rest.response.DocumentDto
import com.freight24.rest.response.TruckDetailDto
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

fun TruckDetailDto.toDomain(): TruckDetail {
    return TruckDetail(
        truckId = truckId,
        registrationNumber = registrationNumber,
        isVerified = isVerified,
        truckType = truckType,
        bodyType = bodyType,
        make = make,
        model = model,
        rcCard = rcCard?.toDomain(),
        fcCertificate = fcCertificate?.toDomain(),
        insurance = insurance?.toDomain(),
        isBlacklisted = isBlacklisted
    )
}

fun DocumentDto.toDomain(): TruckDocument {
    return TruckDocument(
        url = url,
        status = status
    )
}
