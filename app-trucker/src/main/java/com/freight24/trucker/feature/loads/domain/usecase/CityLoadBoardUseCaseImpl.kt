package com.freight24.trucker.feature.loads.domain.usecase

import com.freight24.common.model.PaginatedLoads
import com.freight24.rest.response.PaginatedLoadBoardResponse
import com.freight24.trucker.feature.loads.data.repository.LoadBoardRepository
import com.freight24.trucker.feature.loads.domain.mapper.toLoad
import javax.inject.Inject

class CityLoadBoardUseCaseImpl
    @Inject
    constructor(
        private val loadBoardRepository: LoadBoardRepository,
    ) : CityLoadBoardUseCase {
        override suspend fun invoke(params: CityLoadBoardUseCaseParams): Result<PaginatedLoads> {
            return runCatching {
                val response = fetchLoadsForCity(params.cityId, params.page, params.forceRefresh)
                val loads = response.data?.loads.orEmpty()
                val pagination = response.data?.pagination

                PaginatedLoads(
                    loads = loads.map { it.toLoad(fallbackCity = it.operationalCity ?: "--") },
                    page = pagination?.page ?: 0,
                    hasMore = pagination?.hasMore ?: false,
                    totalLoads = pagination?.totalLoads ?: 0,
                )
            }
        }

        private suspend fun fetchLoadsForCity(
            cityId: Int,
            page: Int,
            forceRefresh: Boolean,
        ): PaginatedLoadBoardResponse = loadBoardRepository.fetchLoadsForCity(cityId, page, forceRefresh).getOrThrow()
    }
