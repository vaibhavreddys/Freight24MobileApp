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
                val fallbackCity =
                    response.data?.loads?.firstOrNull()?.loadingCity
                        ?: response.data?.loads?.firstOrNull()?.loadingArea
                        ?: "--"

                PaginatedLoads(
                    loads = response.data?.loads?.map { it.toLoad(fallbackCity) } ?: emptyList(),
                    page = response.data?.pagination?.page ?: 0,
                    hasMore = response.data?.pagination?.has_more ?: false,
                    totalLoads = response.data?.pagination?.total_loads ?: 0,
                )
            }
        }

        private suspend fun fetchLoadsForCity(
            cityId: Int,
            page: Int,
            forceRefresh: Boolean,
        ): PaginatedLoadBoardResponse = loadBoardRepository.fetchLoadsForCity(cityId, page, forceRefresh).getOrThrow()
    }
