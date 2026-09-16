package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.connect.repository.IndentRepository
import com.freight24.rest.request.UpdateIndentGroupRequest
import javax.inject.Inject

data class UpdateIndentGroupUseCaseParams(
    val groupId: String,
    val request: UpdateIndentGroupRequest,
)

/**
 * Group-level indent update. The only way to change an indent's contact roles.
 * Rejected by the backend unless the group is still `OPEN` and the caller is the
 * group's creator or an admin / operations manager.
 *
 * Carries no payload: the route responds with the raw `indent_groups` row, and
 * the group write has already committed by then, so the caller only needs to
 * know whether it succeeded.
 */
interface UpdateIndentGroupUseCase {
    suspend fun invoke(params: UpdateIndentGroupUseCaseParams): Result<Unit>
}

class UpdateIndentGroupUseCaseImpl
    @Inject
    constructor(
        private val repository: IndentRepository,
    ) : UpdateIndentGroupUseCase {
        override suspend fun invoke(
            params: UpdateIndentGroupUseCaseParams,
        ): Result<Unit> = repository.updateIndentGroup(params.groupId, params.request)
    }
