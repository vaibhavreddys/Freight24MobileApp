package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.IndentRepository
import com.freight24.rest.request.IndentLifecycleRequest
import javax.inject.Inject

data class ReopenIndentUseCaseParams(
    val id: String,
    val reasonCode: String,
    val remarks: String?,
) : UseCaseParams

interface ReopenIndentUseCase : UseCase<ReopenIndentUseCaseParams, Unit>

class ReopenIndentUseCaseImpl
    @Inject
    constructor(
        private val repository: IndentRepository,
    ) : ReopenIndentUseCase {
        override suspend fun invoke(params: ReopenIndentUseCaseParams): Result<Unit> =
            repository.reopenIndent(
                params.id,
                IndentLifecycleRequest(params.reasonCode, params.remarks),
            )
    }
