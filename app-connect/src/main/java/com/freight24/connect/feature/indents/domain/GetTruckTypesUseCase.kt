package com.freight24.connect.feature.indents.domain

import com.freight24.connect.repository.IndentRepository
import com.freight24.rest.response.TruckCategoryDto
import javax.inject.Inject

interface GetTruckTypesUseCase {
    suspend fun invoke(): Result<TruckCategoryDto>
}

class GetTruckTypesUseCaseImpl
    @Inject
    constructor(
        private val repository: IndentRepository,
    ) : GetTruckTypesUseCase {
        override suspend fun invoke(): Result<TruckCategoryDto> {
            return repository.getTruckTypes()
        }
    }
