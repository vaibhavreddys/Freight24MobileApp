@file:kotlinx.serialization.UseSerializers(com.freight24.rest.serialization.BigDecimalSerializer::class)

package com.freight24.rest.response

import java.math.BigDecimal
import kotlinx.serialization.Serializable

@Serializable
data class ClientPaymentBreakdown(
    val clientNetAmount: BigDecimal? = null,
    val clientPaidAmount: BigDecimal? = null,
    val clientCreditAmount: BigDecimal? = null,
    val clientPendingAmount: BigDecimal? = null,
    val advanceExpectedAmount: BigDecimal? = null,
    val advanceReceivedAmount: BigDecimal? = null,
    val advancePendingAmount: BigDecimal? = null,
    val balanceExpectedAmount: BigDecimal? = null,
    val balanceReceivedAmount: BigDecimal? = null,
    val balancePendingAmount: BigDecimal? = null,
    val isPaymentCompleted: Boolean = false
)
