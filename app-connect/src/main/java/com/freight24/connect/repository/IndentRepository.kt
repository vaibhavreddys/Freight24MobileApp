package com.freight24.connect.repository

import com.freight24.rest.request.CreateIndentRequest
import com.freight24.rest.response.BaseResponse
import com.freight24.rest.response.ClientDto
import com.freight24.rest.response.LocationDto
import com.freight24.rest.response.TruckTypeDto

interface IndentRepository {
    suspend fun searchLocations(query: String): Result<List<LocationDto>>
    suspend fun searchClients(query: String): Result<List<ClientDto>>
    suspend fun getTruckTypes(): Result<List<TruckTypeDto>>
    suspend fun createIndent(request: CreateIndentRequest): Result<BaseResponse>
}
