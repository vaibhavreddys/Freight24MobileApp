package com.freight24.connect.feature.inbox.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.InterestInboxRepository
import com.freight24.rest.response.BaseResponse
import javax.inject.Inject
import kotlin.String

data class WithDrawInterestUseCaseParams(
    val interestId: String,
    val remarks: String?
): UseCaseParams

interface WithDrawInterestUseCase : UseCase<WithDrawInterestUseCaseParams, BaseResponse>

class WithDrawInterestUseCaseImpl @Inject constructor(
    private val repository: InterestInboxRepository
) : WithDrawInterestUseCase {
    override suspend fun invoke(params: WithDrawInterestUseCaseParams): Result<BaseResponse> {
        return repository.withdrawInterest(
            interestId = params.interestId,
            remarks = params.remarks ?: ""
        )
    }
}