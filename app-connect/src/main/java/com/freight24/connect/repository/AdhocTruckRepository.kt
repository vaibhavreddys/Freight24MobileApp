package com.freight24.connect.repository

import com.freight24.rest.response.AdhocTruckOwnerDto
import com.freight24.rest.request.CreateAdhocTruckRequest

interface AdhocTruckRepository {
    suspend fun searchTruckOwnersByPhone(phone: String): Result<List<AdhocTruckOwnerDto>>
    suspend fun createAdhocTruckEntry(request: CreateAdhocTruckRequest): Result<Unit>
}
