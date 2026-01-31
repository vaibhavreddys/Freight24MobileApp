package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.connect.repository.LocationRepository
import com.freight24.rest.response.LocationDto
import javax.inject.Inject

interface GetLocationsUseCase {
    suspend fun invoke(query: String): Result<List<LocationDto>>
}

class GetLocationsUseCaseImpl
    @Inject
    constructor(
        private val repository: LocationRepository,
    ) : GetLocationsUseCase {
        override suspend fun invoke(query: String): Result<List<LocationDto>> {
            return repository.searchLocations(query)
        }
    }
