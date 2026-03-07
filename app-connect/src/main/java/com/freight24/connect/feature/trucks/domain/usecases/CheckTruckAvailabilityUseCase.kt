package com.freight24.connect.feature.trucks.domain.usecases

import com.freight24.connect.repository.TruckRepository
import javax.inject.Inject

class CheckTruckAvailabilityUseCase @Inject constructor(
    private val repository: TruckRepository
) {
    suspend operator fun invoke(registrationNumber: String): Result<String?> {
        return repository.checkTruckAvailability(registrationNumber)
    }
}
