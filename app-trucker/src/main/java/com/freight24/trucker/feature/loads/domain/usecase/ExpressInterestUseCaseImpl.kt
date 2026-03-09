package com.freight24.trucker.feature.loads.domain.usecase

import com.freight24.rest.response.IndentInterestResponse
import com.freight24.trucker.feature.loads.data.repository.LoadBoardRepository
import javax.inject.Inject

class ExpressInterestUseCaseImpl @Inject constructor(
    private val repository: LoadBoardRepository,
) : ExpressInterestUseCase {
    override suspend fun invoke(params: ExpressInterestUseCaseParams): Result<IndentInterestResponse> {
        return repository.expressInterest(params.indentId)
    }
}
