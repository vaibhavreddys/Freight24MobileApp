@file:kotlinx.serialization.UseSerializers(com.freight24.rest.serialization.BigDecimalSerializer::class)

package com.freight24.rest.response

import java.math.BigDecimal
import kotlinx.serialization.Serializable

@Serializable
data class PaymentTransactionsResponse(
    val success: Boolean,
    val data: List<PaymentTransaction> = emptyList()
)

@Serializable
data class PaymentTransaction(
    val id: String,
    val paymentType: String? = null,
    val paymentDirection: String? = null,
    val counterpartyType: String? = null,
    val amount: BigDecimal = BigDecimal.ZERO,
    val paymentMethod: String? = null,
    val paymentReference: String? = null,
    val paymentStatus: String? = null,
    val paymentDate: String? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    val verifiedAt: String? = null,
    val verifiedBy: PaymentTransactionUser? = null
)

@Serializable
data class PaymentTransactionUser(
    val id: String,
    val name: String? = null
)
