package com.freight24.trucker.feature.trucks.domain.usecase

import com.freight24.common.model.Truck
import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.EmptyUseCaseParams

interface FetchTrucksUseCase : UseCase<EmptyUseCaseParams, List<Truck>>
