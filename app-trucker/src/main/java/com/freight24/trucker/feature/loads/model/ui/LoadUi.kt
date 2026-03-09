package com.freight24.trucker.feature.loads.model.ui

data class LoadUi(
    val id: String,
    val sourceCity: String,
    val destinationCity: String,
    val postedTime: String,
    val price: String,
    val truckInfo: String,
    val loadWeight: String,
    val commodity: String,
    val f24AgentName: String,
    val f24AgentPhone: String,
    val isInterested: Boolean = false,
    val isInterestLoading: Boolean = false,
)
