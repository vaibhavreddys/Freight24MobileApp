package com.freight24.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class HaltingRequest(
    val loadingDays: Int? = null,
    val unloadingDays: Int? = null,
    val ratePerDay: Int? = null,
    val notes: String? = null
)
