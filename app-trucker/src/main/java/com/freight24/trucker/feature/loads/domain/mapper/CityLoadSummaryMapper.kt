package com.freight24.trucker.feature.loads.domain.mapper

import com.freight24.common.model.CityLoadSummary
import com.freight24.rest.response.LoadBoardResponse
import kotlin.collections.map
import kotlin.collections.orEmpty

fun LoadBoardResponse.toCityLoadSummary(): List<CityLoadSummary> {
    return data.orEmpty().map { dto ->
        CityLoadSummary(
            cityId = dto.operationalCityId,
            cityName = dto.operationalCity,
            totalLoads = dto.totalLoads,
            initialLoads = dto.latestLoads.map { it.toLoad(dto.operationalCity) },
        )
    }
}
