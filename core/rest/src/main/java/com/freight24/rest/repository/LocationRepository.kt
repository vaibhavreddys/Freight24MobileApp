package com.freight24.rest.repository

import com.freight24.rest.response.CityResult

interface LocationRepository {
    suspend fun searchCities(query: String, type: String? = null): Result<List<CityResult>>
}
