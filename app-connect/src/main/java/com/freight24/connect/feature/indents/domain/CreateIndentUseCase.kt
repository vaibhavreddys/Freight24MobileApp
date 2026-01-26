package com.freight24.connect.feature.indents.domain

import com.freight24.connect.repository.IndentRepository
import com.freight24.rest.request.CreateIndentRequest
import com.freight24.rest.response.BaseResponse
import javax.inject.Inject

interface CreateIndentUseCase {
    suspend fun invoke(request: CreateIndentRequest): Result<BaseResponse>
}

class CreateIndentUseCaseImpl @Inject constructor(
    private val repository: IndentRepository
) : CreateIndentUseCase {
    override suspend fun invoke(request: CreateIndentRequest): Result<BaseResponse> {
        return repository.createIndent(request)
    }
}
