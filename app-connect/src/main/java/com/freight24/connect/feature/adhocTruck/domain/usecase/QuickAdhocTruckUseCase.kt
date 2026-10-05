package com.freight24.connect.feature.adhocTruck.domain.usecase

import com.freight24.connect.repository.AdhocTruckRepository
import com.freight24.rest.request.CreateAdhocTruckRequest
import com.freight24.rest.response.AdhocTruckOwnerDto
import javax.inject.Inject

interface QuickAdhocTruckUseCase {
    suspend fun searchByPhone(phone: String): Result<List<AdhocTruckOwnerDto>>

    suspend fun createEntry(request: CreateAdhocTruckRequest): Result<Unit>
}

class QuickAdhocTruckUseCaseImpl
    @Inject
    constructor(
        private val repository: AdhocTruckRepository,
    ) : QuickAdhocTruckUseCase {
        override suspend fun searchByPhone(phone: String): Result<List<AdhocTruckOwnerDto>> {
            return repository.searchTruckOwnersByPhone(phone)
        }

        override suspend fun createEntry(request: CreateAdhocTruckRequest): Result<Unit> {
            return repository.createAdhocTruckEntry(request)
        }
    }
