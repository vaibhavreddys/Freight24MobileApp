package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.IndentRepository
import com.freight24.rest.request.UpdateIndentRequest
import com.freight24.rest.response.IndentResponse
import javax.inject.Inject

data class AssignIndentUseCaseParams(
    val id: String,
    val clientTripCost: Int?,
    val negotiatedTripCost: Int?,
    val notes: String?
) : UseCaseParams

interface AssignIndentUseCase : UseCase<AssignIndentUseCaseParams, IndentResponse>

class AssignIndentUseCaseImpl @Inject constructor(
    private val repository: IndentRepository
) : AssignIndentUseCase {
    override suspend fun invoke(input: AssignIndentUseCaseParams): Result<IndentResponse> {
        val request = UpdateIndentRequest(
            status = "READY_FOR_ASSIGNMENT",
            clientTripCost = input.clientTripCost,
            negotiatedTripCost = input.negotiatedTripCost,
            notes = input.notes
        )
        return repository.updateIndent(input.id, request)
    }
}
