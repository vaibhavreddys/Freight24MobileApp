package com.freight24.connect.feature.trucks.domain.usecases

import com.freight24.connect.repository.TruckRepository
import com.freight24.connect.domain.model.EntityPage
import com.freight24.connect.domain.model.TruckCounts
import com.freight24.connect.domain.model.TruckManagementItem
import javax.inject.Inject

data class GetTrucksParams(
    val query: String? = null,
    val status: String? = null,
    val documentStatus: String? = null,
    val page: Int = 1,
    val limit: Int = 30,
)

interface GetTrucksUseCase {
    suspend operator fun invoke(params: GetTrucksParams = GetTrucksParams()): Result<EntityPage<TruckManagementItem, TruckCounts>>
}

class GetTrucksUseCaseImpl
    @Inject
    constructor(
        private val repository: TruckRepository,
    ) : GetTrucksUseCase {
        override suspend fun invoke(params: GetTrucksParams): Result<EntityPage<TruckManagementItem, TruckCounts>> {
            return repository.getTruckManagementPage(
                query = params.query,
                status = params.status,
                documentStatus = params.documentStatus,
                page = params.page,
                limit = params.limit,
            )
        }
    }
