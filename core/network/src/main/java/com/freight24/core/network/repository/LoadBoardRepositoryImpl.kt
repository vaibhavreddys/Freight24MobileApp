package com.freight24.core.network.repository

import com.freight24.core.common.model.CityLoadSummary
import com.freight24.core.common.model.Load
import com.freight24.core.common.model.PaginatedLoads
import com.freight24.core.common.repository.LoadBoardRepository
import com.freight24.core.network.api.LoadBoardApi
import com.freight24.core.network.mapper.toDomain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadBoardRepositoryImpl @Inject constructor(
    private val api: LoadBoardApi
) : LoadBoardRepository {

    override suspend fun fetchLoadBoardSummary(): List<CityLoadSummary> {
        val response = api.getLoadBoardSummary()
        if (!response.success) return emptyList()

        return response.data.orEmpty().map { city ->
            CityLoadSummary(
                cityId = city.operationalCityId,
                cityName = city.operationalCity,
                totalLoads = city.totalLoads,
                initialLoads = city.latestLoads.map { it.toDomain(city.operationalCity) }
            )
        }
    }

    override suspend fun fetchMoreLoads(
        cityId: Int,
        offset: Int,
        limit: Int
    ): List<Load> {
        // Future API:
        // GET /indents/trucker/load-board?city_id=&offset=&limit=
        // TODO("Hook when backend endpoint is ready")
        return emptyList()
    }

    override suspend fun fetchLoadsForCity(
        cityId: Int,
        page: Int
    ): PaginatedLoads {

        val response = api.getPaginatedLoads(cityId, page)
        if (!response.success || response.data == null) {
            return PaginatedLoads(emptyList(), page, false, 0)
        }

        val fallbackCity =
            response.data.loads.firstOrNull()?.loadingCity
                ?: response.data.loads.firstOrNull()?.loadingArea
                ?: "--"

        return PaginatedLoads(
            loads = response.data.loads.map { it.toDomain(fallbackCity) },
            page = response.data.pagination.page,
            hasMore = response.data.pagination.has_more,
            totalLoads = response.data.pagination.total_loads
        )
    }




}


