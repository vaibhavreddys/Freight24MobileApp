package com.freight24.trucker.feature.trucks.domain.usecase

import com.freight24.common.model.TruckDetail
import com.freight24.trucker.feature.trucks.data.repository.TruckRepository
import com.freight24.trucker.feature.trucks.mapper.toDomain
import javax.inject.Inject

internal class FetchTruckDetailUseCaseImpl
    @Inject
    constructor(
        private val repository: TruckRepository,
    ) : FetchTruckDetailUseCase {
        override suspend fun invoke(params: FetchTruckDetailParams): Result<TruckDetail> {
            return repository.fetchTruckDetail(params.truckId).map { response ->
                response.data?.toDomain() ?: throw Exception("Empty response data")
            }
        }
    }
