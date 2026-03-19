package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.IndentRepository
import com.freight24.rest.request.UpdateIndentRequest
import com.freight24.rest.response.IndentResponse
import javax.inject.Inject

data class CancelIndentUseCaseParams(
    val id: String,
) : UseCaseParams

interface CancelIndentUseCase : UseCase<CancelIndentUseCaseParams, IndentResponse>

class CancelIndentUseCaseImpl @Inject constructor(
    private val repository: IndentRepository
) : CancelIndentUseCase {

    override suspend fun invoke(params: CancelIndentUseCaseParams): Result<IndentResponse> {
        val request = UpdateIndentRequest(
            status = "CANCELLED",
        )
        return repository.updateIndent(params.id, request)
    }

}