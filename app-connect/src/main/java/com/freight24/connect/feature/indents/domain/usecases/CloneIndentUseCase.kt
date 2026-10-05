package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.IndentRepository
import com.freight24.rest.response.IndentResponse
import javax.inject.Inject

data class ClonedIndentUseCaseParams(
    val groupIndentId: String,
    val assignedTo: String?,
    val notes: String?,
) : UseCaseParams

interface CloneIndentUseCase : UseCase<ClonedIndentUseCaseParams, IndentResponse>

class CloneIndentUseCaseImpl
    @Inject
    constructor(
        private val repository: IndentRepository,
    ) : CloneIndentUseCase {
        override suspend fun invoke(params: ClonedIndentUseCaseParams): Result<IndentResponse> {
            return repository.cloneIndent(
                params.groupIndentId,
                params.assignedTo,
                params.notes,
            )
        }
    }
