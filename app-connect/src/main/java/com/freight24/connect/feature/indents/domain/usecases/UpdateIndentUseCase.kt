package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.IndentRepository
import com.freight24.rest.request.CreateIndentRequest
import com.freight24.rest.request.UpdateIndentRequest
import com.freight24.rest.response.IndentResponse
import javax.inject.Inject

data class UpdateIndentUseCaseParams(
    val id: String,
    val request: UpdateIndentRequest
) : UseCaseParams

interface UpdateIndentUseCase : UseCase<UpdateIndentUseCaseParams, IndentResponse>

class UpdateIndentUseCaseImpl @Inject constructor(
    private val repository: IndentRepository
) : UpdateIndentUseCase {
    override suspend fun invoke(params: UpdateIndentUseCaseParams): Result<IndentResponse> {
        return repository.updateIndent(params.id, params.request)
    }
}
