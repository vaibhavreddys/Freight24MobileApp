package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.IndentRepository
import com.freight24.rest.response.GetIndentsResponse
import javax.inject.Inject

data class GetAllIndentsUseCaseParams(
    val page: Int,
    val limit: Int,
    val inactive: Boolean = false,
) : UseCaseParams

interface GetAllIndentsUseCase : UseCase<GetAllIndentsUseCaseParams, GetIndentsResponse>

class GetAllIndentsUseCaseImpl
    @Inject
    constructor(
        private val repository: IndentRepository,
    ) : GetAllIndentsUseCase {
        override suspend fun invoke(params: GetAllIndentsUseCaseParams): Result<GetIndentsResponse> {
            return if (params.inactive) {
                repository.getInactiveIndents(params.page, params.limit)
            } else {
                repository.getAllIndents(params.page, params.limit)
            }
        }
    }
