package com.freight24.connect.repository

import com.freight24.rest.response.CityResult
import com.freight24.rest.response.LocationDto

interface LocationRepository : com.freight24.rest.repository.LocationRepository {
    suspend fun searchLocations(query: String): Result<List<LocationDto>>
    override suspend fun searchCities(query: String, type: String?): Result<List<CityResult>>
}
