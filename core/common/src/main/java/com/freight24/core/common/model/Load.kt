package com.freight24.core.common.model

data class Load(
    val id: String,
    val sourceCity: String,
    val destinationCity: String,
    val material: String,
    val weight: String,
    val truckInfo: String,
    val price: String,
    val createdTime: String,
    val agentName: String,
    val agentPhone: String
)
