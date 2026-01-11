package com.freight24.core.network.mapper

import com.freight24.core.common.model.StatusEvent
import com.freight24.core.common.model.Trip
import com.freight24.core.common.model.TripDetail
import com.freight24.core.common.model.TripLocation
import com.freight24.core.common.model.TripDocument
import com.freight24.core.common.util.DateTimeUtils
import com.freight24.core.common.util.TruckNameFormatter
import com.freight24.core.network.model.StatusEventDto
import com.freight24.core.network.model.TripDetailDto
import com.freight24.core.network.model.TripDocumentDto
import com.freight24.core.network.model.TripDto
import com.freight24.core.network.model.TripLocationDto

fun TripDto.toDomain(): Trip = Trip(
    id = id,
    registrationNumber = registration_number,
    truckType = TruckNameFormatter.shortenTruckName(truck_type),
    status = trucker_status,
    createdAt = DateTimeUtils.formatRelativeTime(created_at),
    tripCost = trucker_trip_cost,
    advanceAmount = advance_amount,
    origin = origin.toDisplayString(),
    destination = destination.toDisplayString()
)

fun TripDetailDto.toDomain(): TripDetail = TripDetail(
    id = id,
    tripCode = trip_code,
    registrationNumber = registration_number,
    truckType = TruckNameFormatter.shortenTruckName(truck_type),
    createdAt = DateTimeUtils.formatRelativeTime(created_at),
    tripCost = trucker_trip_cost,
    advanceAmount = advance_amount,
    origin = origin.toDomain(),
    destination = destination.toDomain(),
    statusEvents = status_events.map { it.toDomain() },
    tripDocuments = trip_documents?.map { it.toDomain() } ?: emptyList()
)

fun TripDocumentDto.toDomain(): TripDocument = TripDocument(
    id = id,
    documentType = document_type,
    originalFilename = original_filename,
    verificationStatus = verification_status,
    uploadedAt = uploaded_at,
    podType = pod_type,
    podStatus = pod_status
)

fun TripLocationDto.toDomain(): TripLocation = TripLocation(
    city = city,
    area = area,
    address = address
)

fun StatusEventDto.toDomain(): StatusEvent = StatusEvent(
    eventType = event_type,
    eventBy = event_by,
    eventAt = event_at,
    details = details
)

private fun TripLocationDto.toDisplayString(): String {
    return city ?: area ?: address ?: ""
}
