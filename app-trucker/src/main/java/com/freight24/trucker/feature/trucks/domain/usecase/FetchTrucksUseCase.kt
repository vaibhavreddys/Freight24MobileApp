package com.freight24.trucker.feature.trucks.domain.usecase

import com.freight24.common.model.Truck
import com.freight24.common.usecase.EmptyUseCaseParams
import com.freight24.common.usecase.UseCase

interface FetchTrucksUseCase : UseCase<EmptyUseCaseParams, List<Truck>>
