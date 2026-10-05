package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.IndentRepository
import com.freight24.rest.response.IndentResponse
import javax.inject.Inject

data class GetIndentByIdUseCaseParams(
    val identId: String,
) : UseCaseParams

interface GetIndentByIdUseCase : UseCase<GetIndentByIdUseCaseParams, IndentResponse>

class GetIndentByIdUseCaseImpl
    @Inject
    constructor(
        private val repository: IndentRepository,
    ) : GetIndentByIdUseCase {
        override suspend fun invoke(params: GetIndentByIdUseCaseParams): Result<IndentResponse> {
            return repository.getIndentById(params.identId)
        }
    }
