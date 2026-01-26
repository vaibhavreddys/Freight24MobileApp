package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class TruckTypeDto(
    val id: String,
    val name: String,
    val category: String, // "Open" or "Closed"
    val capacity: Double? = null
)
