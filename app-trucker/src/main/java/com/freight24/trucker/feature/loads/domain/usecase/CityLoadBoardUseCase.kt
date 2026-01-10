package com.freight24.trucker.feature.loads.domain.usecase

import com.freight24.common.model.PaginatedLoads
import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams

data class CityLoadBoardUseCaseParams(
    val cityId: Int,
    val page: Int,
    val forceRefresh: Boolean = false,
) : UseCaseParams

interface CityLoadBoardUseCase : UseCase<CityLoadBoardUseCaseParams, PaginatedLoads>
