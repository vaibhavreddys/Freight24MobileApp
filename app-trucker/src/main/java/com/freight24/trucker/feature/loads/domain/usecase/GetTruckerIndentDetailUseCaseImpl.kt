package com.freight24.trucker.feature.loads.domain.usecase

import com.freight24.rest.response.TruckerIndentDetailResponse
import com.freight24.trucker.feature.loads.data.repository.LoadBoardRepository
import javax.inject.Inject

class GetTruckerIndentDetailUseCaseImpl @Inject constructor(
    private val repository: LoadBoardRepository,
) : GetTruckerIndentDetailUseCase {
    override suspend fun invoke(params: GetTruckerIndentDetailParams): Result<TruckerIndentDetailResponse> {
        return repository.fetchTruckerIndentDetail(params.indentId)
    }
}
