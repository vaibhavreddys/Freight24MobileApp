package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.connect.repository.IndentRepository
import com.freight24.rest.request.CreateIndentRequest
import com.freight24.rest.response.IndentResponse
import javax.inject.Inject

interface CreateIndentUseCase {
    suspend fun invoke(request: CreateIndentRequest): Result<IndentResponse>
}

class CreateIndentUseCaseImpl
    @Inject
    constructor(
        private val repository: IndentRepository,
    ) : CreateIndentUseCase {
        override suspend fun invoke(request: CreateIndentRequest): Result<IndentResponse> {
            return repository.createIndent(request)
        }
    }
