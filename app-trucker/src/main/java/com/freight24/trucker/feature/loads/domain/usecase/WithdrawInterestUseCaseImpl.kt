package com.freight24.trucker.feature.loads.domain.usecase

import com.freight24.rest.response.IndentInterestResponse
import com.freight24.trucker.feature.loads.data.repository.LoadBoardRepository
import javax.inject.Inject

class WithdrawInterestUseCaseImpl @Inject constructor(
    private val repository: LoadBoardRepository,
) : WithdrawInterestUseCase {
    override suspend fun invoke(params: WithdrawInterestUseCaseParams): Result<IndentInterestResponse> {
        return repository.withdrawInterest(params.indentId)
    }
}
