package com.freight24.connect.feature.trucks.domain.usecases

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.TruckRepository
import com.freight24.connect.domain.model.EntityPage
import com.freight24.connect.domain.model.TruckAssignmentOption
import com.freight24.connect.domain.model.TruckCounts
import javax.inject.Inject

data class SearchTrucksUseCaseParams(
    val query: String,
    val page: Int = 1,
    val limit: Int = 30,
) : UseCaseParams

interface SearchTruckUseCase : UseCase<SearchTrucksUseCaseParams, EntityPage<TruckAssignmentOption, TruckCounts>>

class SearchTruckUseCaseImpl
    @Inject
    constructor(
        private val repository: TruckRepository,
    ) : SearchTruckUseCase {
        override suspend fun invoke(params: SearchTrucksUseCaseParams): Result<EntityPage<TruckAssignmentOption, TruckCounts>> {
            return repository.searchAssignmentTrucks(params.query, params.page, params.limit)
        }
    }
