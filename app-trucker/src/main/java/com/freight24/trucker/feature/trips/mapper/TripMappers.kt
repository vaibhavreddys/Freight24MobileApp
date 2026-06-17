package com.freight24.trucker.feature.trips.mapper

import com.freight24.common.model.AdvanceBlock
import com.freight24.common.model.BalanceBlock
import com.freight24.common.model.FeeBlock
import com.freight24.common.model.HaltingBlock
import com.freight24.common.model.HaltingLeg
import com.freight24.common.model.LuBlock
import com.freight24.common.model.LuLeg
import com.freight24.common.model.StatusEvent
import com.freight24.common.model.TdsBlock
import com.freight24.common.model.Trip
import com.freight24.common.model.TripDetail
import com.freight24.common.model.TripDocument
import com.freight24.common.model.TripFinancials
import com.freight24.common.model.TripLocation
import com.freight24.common.util.DateTimeUtils
import com.freight24.common.util.NameFormatter
import com.freight24.rest.response.AdvanceBlockDto
import com.freight24.rest.response.BalanceBlockDto
import com.freight24.rest.response.FeeBlockDto
import com.freight24.rest.response.HaltingBlockDto
import com.freight24.rest.response.HaltingLegDto
import com.freight24.rest.response.LuBlockDto
import com.freight24.rest.response.LuLegDto
import com.freight24.rest.response.StatusEventDto
import com.freight24.rest.response.TdsBlockDto
import com.freight24.rest.response.TripDetailDto
import com.freight24.rest.response.TripDocumentDto
import com.freight24.rest.response.TripDto
import com.freight24.rest.response.TripFinancialsDto
import com.freight24.rest.response.TripLocationDto

fun TripDto.toDomain(): Trip {
    return Trip(
        id = id,
        registrationNumber = registration_number,
        truckType = NameFormatter.shortenTruckName(truck_type),
        status = trucker_status,
        createdAt = DateTimeUtils.formatRelativeTime(created_at),
        tripCost = trucker_trip_cost,
        totalPaidAmount = trucker_paid_amount,
        origin = origin.toDisplayString(),
        destination = destination.toDisplayString(),
    )
}

fun TripDetailDto.toDomain(): TripDetail {
    val mappedFinancials = financials?.toDomain()
    val latestDocuments = trip_documents
        .groupBy { it.document_type }
        .map { (_, docs) -> docs.maxByOrNull { it.uploaded_at ?: "" }!! }
        .map { it.toDomain() }
    return TripDetail(
        id = id,
        tripCode = trip_code,
        registrationNumber = registration_number.orEmpty(),
        truckType = NameFormatter.shortenTruckName(truck_type),
        createdAt = DateTimeUtils.formatRelativeTime(created_at),
        tripCost = mappedFinancials?.truckerTripCost ?: 0.0,
        totalPaidAmount = mappedFinancials?.totalPaidAmount ?: 0.0,
        status = NameFormatter.formatTripStatusMessage(trucker_status),
        origin = origin.toDomain(),
        destination = destination.toDomain(),
        statusEvents = status_events.map { it.toDomain() },
        tripDocuments = latestDocuments,
        financials = mappedFinancials,
        truckerStatus = trucker_status,
    )
}

fun TripFinancialsDto.toDomain(): TripFinancials {
    return TripFinancials(
        truckerTripCost = trucker_trip_cost,
        platformFee = platform_fee.toDomain(),
        tds = tds.toDomain(),
        halting = halting.toDomain(),
        luCharges = lu_charges.toDomain(),
        otherDeductions = other_deductions,
        deductionRemarks = deduction_remarks,
        advance = advance.toDomain(),
        balance = balance.toDomain(),
        netPayable = net_payable,
        totalPaidAmount = total_paid_amount,
    )
}

fun FeeBlockDto.toDomain(): FeeBlock = FeeBlock(amount = amount, percent = percent)

fun TdsBlockDto.toDomain(): TdsBlock = TdsBlock(amount = amount, percent = percent, applicable = applicable)

fun HaltingBlockDto.toDomain(): HaltingBlock = HaltingBlock(
    loading = loading.toDomain(),
    unloading = unloading.toDomain(),
)

fun HaltingLegDto.toDomain(): HaltingLeg = HaltingLeg(
    days = days,
    totalAmount = totalAmount,
    perDayAmount = perDayAmount,
    notes = notes,
)

fun LuBlockDto.toDomain(): LuBlock = LuBlock(
    loading = loading.toDomain(),
    unloading = unloading.toDomain(),
)

fun LuLegDto.toDomain(): LuLeg = LuLeg(
    id = id,
    total = total,
    reimbursable = reimbursable,
    isLUIncluded = isLUIncluded ?: false,
    slipUrl = slipUrl,
    notes = notes,
)

fun AdvanceBlockDto.toDomain(): AdvanceBlock = AdvanceBlock(
    percent = percent,
    amount = amount,
    paid = paid,
    paidAmount = paidAmount,
)

fun BalanceBlockDto.toDomain(): BalanceBlock = BalanceBlock(
    amount = amount,
    paidAmount = paidAmount,
)

fun TripLocationDto?.toDomain(): TripLocation {
    return TripLocation(
        city = this?.city,
        area = this?.area,
        address = this?.address,
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
        oldStatus = old_status,
        newStatus = new_status,
    )
}

fun TripDocumentDto.toDomain(): TripDocument {
    return TripDocument(
        id = id,
        documentType = document_type,
        verificationStatus = verification_status,
        verificationRemarks = verification_remarks,
        uploadedAt = uploaded_at,
        podType = pod_type,
        podStatus = pod_status,
        uploadedBy = uploaded_by,
        uploadedByName = uploaded_by_name,
    )
}
