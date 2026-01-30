package com.freight24.connect.repository

import com.freight24.rest.response.LocationDto

interface LocationRepository {
    suspend fun searchLocations(query: String): Result<List<LocationDto>>
}
