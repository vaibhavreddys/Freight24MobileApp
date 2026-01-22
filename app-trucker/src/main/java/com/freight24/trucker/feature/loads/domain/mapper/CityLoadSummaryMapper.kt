package com.freight24.trucker.feature.loads.domain.mapper

import com.freight24.common.model.CityLoadSummary
import com.freight24.rest.response.LoadBoardResponse
import kotlin.collections.map
import kotlin.collections.orEmpty

fun LoadBoardResponse.toCityLoadSummary(): List<CityLoadSummary> {
    return data.orEmpty().mapIndexed { index, dto ->
        CityLoadSummary(
            // Use index-based negative ID to guarantee uniqueness among items with null IDs
            cityId = dto.operationalCityId ?: (-(index + 1)),
            cityName = dto.operationalCity ?: "Unknown",
            totalLoads = dto.totalLoads ?: 0,
            initialLoads = dto.latestLoads.orEmpty().map { it.toLoad(dto.operationalCity ?: "Unknown") },
        )
    }
}
