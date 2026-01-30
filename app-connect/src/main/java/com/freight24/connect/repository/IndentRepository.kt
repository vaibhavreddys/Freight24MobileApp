package com.freight24.connect.repository

import com.freight24.rest.request.CreateIndentRequest
import com.freight24.rest.response.BaseResponse
import com.freight24.rest.response.TruckCategoryDto

interface IndentRepository {
    suspend fun getTruckTypes(): Result<TruckCategoryDto>

    suspend fun createIndent(request: CreateIndentRequest): Result<BaseResponse>
}
