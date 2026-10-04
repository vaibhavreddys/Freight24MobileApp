@file:kotlinx.serialization.UseSerializers(com.freight24.rest.serialization.BigDecimalSerializer::class)

package com.freight24.rest.response

import java.math.BigDecimal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TripDataResponse(
    val id: String,
    val indentId: String,
    val tripCode: String,
    val createdAt: String,
    val driverName: String?,
    val driverPhone: String?,
    val clientStatus: String,
    val indentGroupId: String,
    val status: String,
    val truckerStatus: String,
    val clientTripCost: BigDecimal? = null,
    val truckerTripCost: String?,
    val truckerNetAmount: String?,
    val indentDetails: IndentDetails,
    val client: Client,
    val trucker: Trucker? = null,
    val truck: Truck,
    val loadingLocation: Location,
    val unLoadingLocation: Location,
    val financials: FinanceDetails? = null,
    val documents: List<Document>? = null,
    @SerialName("timeline") val timelines: List<Timeline>? = null,
    val clientPaymentBreakdown: ClientPaymentBreakdown? = null,
) {

    @Serializable
    data class Truck(
        val id: String,
        val registrationNumber: String,
        val truckTypeId: String,
        val truckType: TruckType,
    )

    @Serializable
    data class TruckType(
        val bodyType: String,
        val truckType: String,
        val displayName: String,
    )

    @Serializable
    data class Client(
        val id: String,
        val name: String? = null,
        val companyName: String? = null,
        val displayName: String? = null,
        val tagCode: String? = null,
        val contacts: Contacts? = null,
    )

    @Serializable
    data class Trucker(
        val id: String? = null,
        val name: String? = null,
        val phone: String? = null,
        val tagCode: String? = null,
    )

    @Serializable
    data class Contacts(
        val placement: Contact? = null,
        val pod: Contact? = null,
        val payment: Contact? = null,
        val operations: Contact? = null,
    )

    @Serializable
    data class Contact(
        val id: String? = null,
        val contactId: String? = null,
        val name: String? = null,
        val phone: String? = null,
        val email: String? = null,
    )

    @Serializable
    data class IndentDetails(
        val material: String,
        val priority: String,
        val tatHours: Int?,
        /** Backend column is `DECIMAL(10,2)` — fractional weights such as `2.5` are valid. */
        val loadWeight: Double,
        val tatEnabled: Boolean,
        val flooringType: String?,
        val vehicleHeight: Double?,
        val tarpaulingRequired: Boolean,
        val truckTypeId: String,
        val specialRequirements: String?,
        val truckType: TruckType?,
    )

    @Serializable
    data class Location(
        val id: String,
        val area: String?,
        val city: String?
    )

    @Serializable
    data class FinanceDetails(
        val netPayable: Double,
        val clientTripCost: Double,
        val otherDeductions: BigDecimal? = null,
        val deductionRemarks: String? = null,
        val truckerTripCost: Double,
        val totalPaidAmount: Double,
        val tds: TDS,
        val advance: Advance,
        val balance: Balance,
        val halting: Halting,
        val platformFee: PlatformFee,
        val luCharges: LuCharges,
        val disputeResolved: Boolean = false,
        val disputeResolutionAmount: BigDecimal? = null,
    )

    @Serializable
    data class TDS(
        val amount: Double,
        val percent: Float,
        val applicable: Boolean
    )

    @Serializable
    data class Advance(
        val paid: Boolean,
        val amount: Double,
        val percent: Int,
        val paidAmount: Double
    )

    @Serializable
    data class Balance(
        val amount: Double,
        val paidAmount: Double
    )

    @Serializable
    data class Halting(
        val loading: HaltingCharges,
        val unloading: HaltingCharges,
    )

    @Serializable
    data class HaltingCharges(
        val days: Int,
        val totalAmount: BigDecimal? = null,
        val perDayAmount: BigDecimal? = null,
        val notes: String? = null,
    )

    @Serializable
    data class PlatformFee(
        val amount: Double,
        val percent: Int
    )

    @Serializable
    data class LuCharges(
        val loading: Charges,
        val unloading: Charges,
    )

    @Serializable
    data class Charges(
        val id: String? = null,
        val total: Int,
        val reimbursable: BigDecimal? = null,
        val includedInTripCost: Boolean = false,
        val slipUrl: String? = null,
        val notes: String? = null,
    )

    @Serializable
    data class Document(
        val id: String,
        val type: String,
        val url: String,
        val status: String,
        val podType: String?,
        val podStatus: String?,
    )

    @Serializable
    data class UserRef(
        val id: String? = null,
        val name: String? = null,
    )

    @Serializable
    data class Timeline(
        val isCurrent: Boolean,
        val updatedAt: String? = null,
        val updatedBy: UserRef? = null,
        val statusType: String,
        val isCompleted: Boolean,
    )

}
