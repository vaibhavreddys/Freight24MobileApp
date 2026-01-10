package com.freight24.common.model

data class CityLoadSummary(
    val cityId: Int,
    val cityName: String,
    val totalLoads: Int,
    val initialLoads: List<Load>,
)
