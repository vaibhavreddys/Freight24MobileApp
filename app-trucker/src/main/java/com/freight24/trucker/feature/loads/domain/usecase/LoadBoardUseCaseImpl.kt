package com.freight24.trucker.feature.loads.domain.usecase

import com.freight24.common.model.CityLoadSummary
import com.freight24.common.usecase.EmptyUseCaseParams
import com.freight24.trucker.feature.loads.data.repository.LoadBoardRepository
import com.freight24.trucker.feature.loads.domain.mapper.toCityLoadSummary
import javax.inject.Inject

class LoadBoardUseCaseImpl
    @Inject
    constructor(
        private val repository: LoadBoardRepository,
    ) : LoadBoardUseCase {
        override suspend fun invoke(params: EmptyUseCaseParams): Result<List<CityLoadSummary>> {
            return repository
                .fetchLoadBoardSummary()
                .map {
                    it.toCityLoadSummary()
                }
        }
    }
