package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class TripDetailResponse(
    val success: Boolean,
    val data: TripDetailDto?,
    val message: String? = null,
)

@Serializable
data class TripDetailDto(
    val id: String,
    val trip_code: String,
    val registration_number: String? = null,
    val truck_type: String? = null,
    val created_at: String,
    val trucker_status: String? = null,
    val origin: TripLocationDto? = null,
    val destination: TripLocationDto? = null,
    val status_events: List<StatusEventDto> = emptyList(),
    val trip_documents: List<TripDocumentDto> = emptyList(),
    val financials: TripFinancialsDto? = null,
)

@Serializable
data class StatusEventDto(
    val event_type: String,
    val event_by: String,
    val event_at: String,
    val details: String? = null,
    val old_status: String? = null,
    val new_status: String? = null,
)

@Serializable
data class TripDocumentDto(
    val id: String,
    val document_type: String,
    val file_url: String? = null,
    val verification_status: String,
    val verification_remarks: String? = null,
    val pod_type: String? = null,
    val pod_status: String? = null,
    val uploaded_at: String? = null,
    val uploaded_by: String? = null,
    val uploaded_by_name: String? = null,
)

@Serializable
data class TripFinancialsDto(
    val trucker_trip_cost: Double = 0.0,
    val platform_fee: FeeBlockDto = FeeBlockDto(),
    val tds: TdsBlockDto = TdsBlockDto(),
    val halting: HaltingBlockDto = HaltingBlockDto(),
    val lu_charges: LuBlockDto = LuBlockDto(),
    val other_deductions: Double = 0.0,
    val deduction_remarks: String? = null,
    val advance: AdvanceBlockDto = AdvanceBlockDto(),
    val balance: BalanceBlockDto = BalanceBlockDto(),
    val net_payable: Double = 0.0,
    val total_paid_amount: Double = 0.0,
)

@Serializable
data class FeeBlockDto(
    val amount: Double = 0.0,
    val percent: Int = 0,
)

@Serializable
data class TdsBlockDto(
    val amount: Double = 0.0,
    val percent: Float = 0f,
    val applicable: Boolean = false,
)

@Serializable
data class HaltingBlockDto(
    val loading: HaltingLegDto = HaltingLegDto(),
    val unloading: HaltingLegDto = HaltingLegDto(),
)

@Serializable
data class HaltingLegDto(
    val days: Int = 0,
    val totalAmount: Double = 0.0,
    val perDayAmount: Double = 0.0,
    val notes: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
)

@Serializable
data class LuBlockDto(
    val loading: LuLegDto = LuLegDto(),
    val unloading: LuLegDto = LuLegDto(),
)

@Serializable
data class LuLegDto(
    val id: String? = null,
    val total: Double = 0.0,
    val reimbursable: Double = 0.0,
    val isLUIncluded: Boolean? = null,
    val slipUrl: String? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
)

@Serializable
data class AdvanceBlockDto(
    val percent: Int = 0,
    val amount: Double = 0.0,
    val paid: Boolean = false,
    val paidAmount: Double = 0.0,
)

@Serializable
data class BalanceBlockDto(
    val amount: Double = 0.0,
    val paid: Boolean = false,
    val paidAmount: Double = 0.0,
)
