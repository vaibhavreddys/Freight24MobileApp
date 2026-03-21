package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.InterestInboxRepository
import com.freight24.rest.response.TruckerInboxInterestResponse
import javax.inject.Inject


data class TruckerInterestInboxUseCaseParams(
    val indentId: String? = null
): UseCaseParams

interface TruckerInterestInboxUseCase :
    UseCase<TruckerInterestInboxUseCaseParams, TruckerInboxInterestResponse>

class TruckerInterestInboxUseCaseImpl @Inject constructor(
    private val repository: InterestInboxRepository
) : TruckerInterestInboxUseCase {
    override suspend fun invoke(params: TruckerInterestInboxUseCaseParams): Result<TruckerInboxInterestResponse> {
        return repository.getTruckerInterestInbox(params.indentId)
    }
}