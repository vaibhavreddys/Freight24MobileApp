package com.freight24.connect.repository

import com.freight24.rest.request.UpdateProfileRequest
import com.freight24.rest.response.TruckOwnerDto

interface TruckOwnerRepository {
    suspend fun getTruckOwners(): Result<List<TruckOwnerDto>>

    suspend fun searchTruckOwners(query: String): Result<List<TruckOwnerDto>>

    suspend fun createTruckOwner(request: UpdateProfileRequest): Result<Boolean>
}
