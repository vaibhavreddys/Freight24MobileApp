package com.freight24.trucker.feature.trucks.domain.usecase

interface CheckTruckAvailabilityUseCase {
    suspend operator fun invoke(registrationNumber: String): Result<String?>
}
