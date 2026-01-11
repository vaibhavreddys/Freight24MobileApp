package com.freight24.trucker.feature.trips.data.model

data class Trip(
    val id: String,
    val registrationNumber: String,
    val truckType: String,
    val status: String,
    val createdAt: String,
    val tripCost: Double,
    val advanceAmount: Double,
    val origin: String,
    val destination: String,
)
