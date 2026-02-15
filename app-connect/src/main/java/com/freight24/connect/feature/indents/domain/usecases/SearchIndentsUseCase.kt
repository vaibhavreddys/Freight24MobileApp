package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.IndentRepository
import com.freight24.rest.response.GetIndentsResponse
import javax.inject.Inject

data class SearchIndentsUseCaseParams(
    val query: String,
    val priority: String?,
    val clientId: String?,
    val assignedToMe: Boolean?,
) : UseCaseParams

interface SearchIndentsUseCase : UseCase<SearchIndentsUseCaseParams, GetIndentsResponse>


class SearchIndentsUseCaseImpl @Inject constructor(
    private val repository: IndentRepository
): SearchIndentsUseCase {

    override suspend fun invoke(params: SearchIndentsUseCaseParams): Result<GetIndentsResponse> {
        return repository.searchIndents(
            params.query,
            params.priority,
            params.clientId,
            params.assignedToMe
        )
    }

}