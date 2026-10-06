package com.freight24.connect.feature.trucks.domain.usecases

import com.freight24.connect.repository.TruckRepository
import com.freight24.connect.domain.model.TruckRegistrationValidation
import javax.inject.Inject

class CheckTruckAvailabilityUseCase
    @Inject
    constructor(
        private val repository: TruckRepository,
    ) {
        suspend operator fun invoke(
            registrationNumber: String,
            excludeTruckId: String? = null,
        ): Result<TruckRegistrationValidation> {
            return repository.validateRegistration(registrationNumber, excludeTruckId)
        }
    }
