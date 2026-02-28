package com.freight24.trucker.feature.trips.mapper

import com.freight24.common.model.StatusEvent
import com.freight24.common.model.Trip
import com.freight24.common.model.TripDetail
import com.freight24.common.model.TripDocument
import com.freight24.common.model.TripLocation
import com.freight24.common.util.DateTimeUtils
import com.freight24.common.util.NameFormatter
import com.freight24.rest.response.StatusEventDto
import com.freight24.rest.response.TripDetailDto
import com.freight24.rest.response.TripDocumentDto
import com.freight24.rest.response.TripDto
import com.freight24.rest.response.TripLocationDto

fun TripDto.toDomain(): Trip {
    return Trip(
        id = id,
        registrationNumber = registration_number,
        truckType = NameFormatter.shortenTruckName(truck_type),
        status = trucker_status,
        createdAt = DateTimeUtils.formatRelativeTime(created_at),
        tripCost = trucker_trip_cost,
        advanceAmount = advance_amount,
        origin = origin.toDisplayString(),
        destination = destination.toDisplayString(),
    )
}

fun TripDetailDto.toDomain(): TripDetail {
    return TripDetail(
        id = id,
        tripCode = trip_code,
        registrationNumber = registration_number,
        truckType = NameFormatter.shortenTruckName(truck_type),
        createdAt = DateTimeUtils.formatRelativeTime(created_at),
        tripCost = trucker_trip_cost,
        advanceAmount = advance_amount,
        status = NameFormatter.formatTripStatusMessage(trucker_status),
        origin = origin.toDomain(),
        destination = destination.toDomain(),
        statusEvents = status_events.map { it.toDomain() },
        tripDocuments = trip_documents.map { it.toDomain() },
    )
}

fun TripLocationDto.toDomain(): TripLocation {
    return TripLocation(
        city = city,
        area = area,
        address = address,
    )
}

fun TripLocationDto.toDisplayString(): String {
    return listOfNotNull(area, city).joinToString(", ").ifEmpty { address ?: "Unknown" }
}

fun StatusEventDto.toDomain(): StatusEvent {
    return StatusEvent(
        eventType = event_type,
        eventBy = event_by,
        eventAt = event_at,
        details = details,
    )
}

fun TripDocumentDto.toDomain(): TripDocument {
    return TripDocument(
        documentType = document_type,
        originalFilename = original_filename,
        verificationStatus = verification_status,
        uploadedAt = uploaded_at,
        podType = pod_type,
        podStatus = pod_status,
        documentId = id,
    )
}
