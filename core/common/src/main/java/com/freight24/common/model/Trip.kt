package com.freight24.common.model

data class Trip(
    val id: String,
    val registrationNumber: String,
    val truckType: String,
    val status: String,
    val createdAt: String,
    val tripCost: Double,
    val totalPaidAmount: Double,
    val origin: String,
    val destination: String,
)

data class TripDetail(
    val id: String,
    val tripCode: String,
    val registrationNumber: String,
    val truckType: String,
    val createdAt: String,
    val tripCost: Double,
    val totalPaidAmount: Double,
    val origin: TripLocation,
    val destination: TripLocation,
    val status: String,
    val statusEvents: List<StatusEvent>,
    val tripDocuments: List<TripDocument>,
    val financials: TripFinancials? = null,
    val truckerStatus: String? = null,
    val truckerPayments: List<TruckerPayment> = emptyList(),
    val indentCreatedBy: TripEmployee? = null,
)

data class TripEmployee(
    val id: String?,
    val name: String?,
    val phone: String?,
    val tagCode: String?,
)

/**
 * A payment made to the driver. [utrNumber] is finance's payment reference —
 * the bank UTR for a transfer, the cheque/UPI id otherwise.
 */
data class TruckerPayment(
    val type: String,
    val amount: Double,
    val method: String?,
    val utrNumber: String?,
    val paidOn: String?,
)

data class TripFinancials(
    val truckerTripCost: Double,
    val platformFee: FeeBlock,
    val tds: TdsBlock,
    val halting: HaltingBlock,
    val luCharges: LuBlock,
    val otherDeductions: Double,
    val deductionRemarks: String?,
    val advance: AdvanceBlock,
    val balance: BalanceBlock,
    val netPayable: Double,
    val totalPaidAmount: Double,
)

data class FeeBlock(val amount: Double, val percent: Int)

data class TdsBlock(val amount: Double, val percent: Float, val applicable: Boolean)

data class HaltingBlock(val loading: HaltingLeg, val unloading: HaltingLeg)

data class HaltingLeg(
    val days: Int,
    val totalAmount: Double,
    val perDayAmount: Double,
    val notes: String?,
)

data class LuBlock(val loading: LuLeg, val unloading: LuLeg)

data class LuLeg(
    val id: String?,
    val total: Double,
    val reimbursable: Double,
    val isLUIncluded: Boolean,
    val slipUrl: String?,
    val notes: String?,
)

data class AdvanceBlock(
    val percent: Int,
    val amount: Double,
    val paid: Boolean,
    val paidAmount: Double,
)

data class BalanceBlock(
    val amount: Double,
    val paidAmount: Double,
)

data class TripLocation(
    val city: String?,
    val area: String?,
    val address: String?,
) {
    fun toDisplayString(): String {
        return listOfNotNull(area, city).joinToString(", ").ifEmpty { address ?: "Unknown" }
    }
}

data class StatusEvent(
    val eventType: String,
    val eventBy: String,
    val eventAt: String,
    val details: String? = null,
    val oldStatus: String? = null,
    val newStatus: String? = null,
)

data class TripDocument(
    val id: String?,
    val documentType: String,
    val verificationStatus: String,
    val verificationRemarks: String? = null,
    val uploadedAt: String? = null,
    val podType: String? = null,
    val podStatus: String? = null,
    val uploadedBy: String? = null,
    val uploadedByName: String? = null,
)
