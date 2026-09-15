package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.connect.repository.IndentRepository
import com.freight24.rest.request.UpdateIndentGroupRequest
import com.freight24.rest.response.IndentResponse
import javax.inject.Inject

data class UpdateIndentGroupUseCaseParams(
    val groupId: String,
    val request: UpdateIndentGroupRequest,
)

/**
 * Group-level indent update. The only way to change an indent's contact roles.
 * Rejected by the backend unless the group is still `OPEN` and the caller is the
 * group's creator or an admin / operations manager.
 */
interface UpdateIndentGroupUseCase {
    suspend fun invoke(params: UpdateIndentGroupUseCaseParams): Result<IndentResponse>
}

class UpdateIndentGroupUseCaseImpl
    @Inject
    constructor(
        private val repository: IndentRepository,
    ) : UpdateIndentGroupUseCase {
        override suspend fun invoke(
            params: UpdateIndentGroupUseCaseParams,
        ): Result<IndentResponse> = repository.updateIndentGroup(params.groupId, params.request)
    }
