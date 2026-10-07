package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.IndentRepository
import com.freight24.rest.request.IndentLifecycleRequest
import javax.inject.Inject

data class CancelIndentUseCaseParams(
    val id: String,
    val reasonCode: String,
    val remarks: String?,
) : UseCaseParams

interface CancelIndentUseCase : UseCase<CancelIndentUseCaseParams, Unit>

class CancelIndentUseCaseImpl
    @Inject
    constructor(
        private val repository: IndentRepository,
    ) : CancelIndentUseCase {
        override suspend fun invoke(params: CancelIndentUseCaseParams): Result<Unit> {
            val request = IndentLifecycleRequest(params.reasonCode, params.remarks)
            return repository.cancelIndent(params.id, request)
        }
    }
