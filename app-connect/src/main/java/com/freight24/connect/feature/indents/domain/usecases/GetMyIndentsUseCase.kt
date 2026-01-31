package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.IndentRepository
import com.freight24.rest.response.GetIndentsResponse
import javax.inject.Inject

data class GetMyIndentsUseCaseParams(
    val status: String,
    val page: Int? = null,
    val limit: Int? = null
) : UseCaseParams

interface GetMyIndentsUseCase : UseCase<GetMyIndentsUseCaseParams, GetIndentsResponse>

class GetMyIndentsUseCaseImpl @Inject constructor(
    private val repository: IndentRepository
) : GetMyIndentsUseCase {

    override suspend fun invoke(params: GetMyIndentsUseCaseParams): Result<GetIndentsResponse> {
        return repository.getMyIndents(params.status, params.page, params.limit)
    }

}