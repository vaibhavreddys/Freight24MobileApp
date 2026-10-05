package com.freight24.connect.feature.trips.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.trips.TripRepository
import com.freight24.rest.response.UpdateOtherDeductionsResponse
import javax.inject.Inject

data class UpdateOtherDeductionsUseCaseDataParams(
    val tripId: String,
    val amount: String,
    val remarks: String?,
) : UseCaseParams

internal interface UpdateOtherDeductionsUseCase : UseCase<UpdateOtherDeductionsUseCaseDataParams, UpdateOtherDeductionsResponse>

class UpdateOtherDeductionsUseCaseImpl
    @Inject
    constructor(
        private val repository: TripRepository,
    ) : UpdateOtherDeductionsUseCase {
        override suspend fun invoke(params: UpdateOtherDeductionsUseCaseDataParams): Result<UpdateOtherDeductionsResponse> {
            return repository.updateOtherDeductions(
                params.tripId,
                params.amount,
                params.remarks,
            )
        }
    }
