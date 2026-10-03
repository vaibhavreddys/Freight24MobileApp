package com.freight24.trucker.feature.loads.domain.mapper

import com.freight24.common.model.CityLoadSummary
import com.freight24.rest.response.LoadBoardResponse
import kotlin.collections.map
import kotlin.collections.orEmpty

fun LoadBoardResponse.toCityLoadSummary(): List<CityLoadSummary> {
return data?.groups.orEmpty().mapIndexed { index, group ->
        CityLoadSummary(
            // Use index-based negative ID to guarantee uniqueness among items with null IDs
            cityId = group.groupId ?: group.operationalCityId ?: (-(index + 1)),
            cityName = cityName,
            totalLoads = group.totalLoads ?: 0,
            initialLoads = group.latestLoads.map { it.toLoad(cityName) },
        )
    }
}
