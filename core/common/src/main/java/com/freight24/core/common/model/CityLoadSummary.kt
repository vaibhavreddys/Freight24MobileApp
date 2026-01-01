package com.freight24.core.common.model

import com.freight24.core.common.model.Load

data class CityLoadSummary(
    val cityId: Int,
    val cityName: String,
    val totalLoads: Int,
    val initialLoads: List<Load>
)
