package com.freight24.core.common.repository

import com.freight24.core.common.model.CityLoadSummary
import com.freight24.core.common.model.Load
import com.freight24.core.common.model.PaginatedLoads

interface LoadBoardRepository {
    suspend fun fetchLoadBoardSummary(): List<CityLoadSummary>

    suspend fun fetchMoreLoads(
        cityId: Int,
        offset: Int,
        limit: Int
    ): List<Load>

    suspend fun fetchLoadsForCity(
        cityId: Int,
        page: Int
    ): PaginatedLoads

}

