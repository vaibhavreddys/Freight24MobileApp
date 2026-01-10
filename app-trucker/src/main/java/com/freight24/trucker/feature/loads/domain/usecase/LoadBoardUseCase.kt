package com.freight24.trucker.feature.loads.domain.usecase

import com.freight24.common.model.CityLoadSummary
import com.freight24.common.usecase.EmptyUseCaseParams
import com.freight24.common.usecase.UseCase

interface LoadBoardUseCase : UseCase<EmptyUseCaseParams, List<CityLoadSummary>>
