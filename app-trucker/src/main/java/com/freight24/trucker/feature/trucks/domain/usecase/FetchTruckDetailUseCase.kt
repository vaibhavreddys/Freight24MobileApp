package com.freight24.trucker.feature.trucks.domain.usecase

import com.freight24.common.model.TruckDetail
import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams

interface FetchTruckDetailUseCase : UseCase<FetchTruckDetailParams, TruckDetail>

data class FetchTruckDetailParams(val truckId: String) : UseCaseParams
