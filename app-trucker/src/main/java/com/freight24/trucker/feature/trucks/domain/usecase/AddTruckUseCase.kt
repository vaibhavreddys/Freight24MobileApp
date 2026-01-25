package com.freight24.trucker.feature.trucks.domain.usecase

import com.freight24.rest.response.AddTruckResponse

interface AddTruckUseCase {
    suspend operator fun invoke(params: AddTruckParams): Result<AddTruckResponse>
}
