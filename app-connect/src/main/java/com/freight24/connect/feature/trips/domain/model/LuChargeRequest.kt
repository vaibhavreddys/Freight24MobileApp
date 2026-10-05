package com.freight24.connect.feature.trips.domain.model

data class LuChargeRequest(
    val loadingAmount: Double?,
    val loadingPaymentMethod: String?,
    val loadingReimbursable: Boolean?,
    val unloadingAmount: Double?,
    val unloadingPaymentMethod: String?,
    val unloadingReimbursable: Boolean?,
    val notes: String?,
)
