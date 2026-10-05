package com.freight24.trucker.feature.trucks.domain.usecase

import com.freight24.trucker.feature.trucks.data.repository.TruckRepository
import javax.inject.Inject

internal class CheckTruckAvailabilityUseCaseImpl
    @Inject
    constructor(
        private val repository: TruckRepository,
    ) : CheckTruckAvailabilityUseCase {
        override suspend fun invoke(registrationNumber: String): Result<String?> {
            return repository.checkTruckAvailability(registrationNumber)
        }
    }
