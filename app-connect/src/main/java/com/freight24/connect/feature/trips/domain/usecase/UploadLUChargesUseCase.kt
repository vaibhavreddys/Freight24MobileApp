package com.freight24.connect.feature.trips.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.feature.trips.domain.model.LuChargeRequest
import com.freight24.connect.repository.trips.TripLUChargesRepository
import com.freight24.rest.response.UpdatedLUResponse
import java.io.File
import javax.inject.Inject

data class UploadLUChargesUseCaseParams(
    val tripId: String,
    val loadingFile: File?,
    val unloadingFile: File?,
    val request: LuChargeRequest,
) : UseCaseParams

internal interface UploadLUChargesUseCase :
    UseCase<UploadLUChargesUseCaseParams, UpdatedLUResponse>

class UploadLUChargesUseCaseImpl
    @Inject
    constructor(
        private val repository: TripLUChargesRepository,
    ) : UploadLUChargesUseCase {
        override suspend fun invoke(params: UploadLUChargesUseCaseParams): Result<UpdatedLUResponse> {
            return repository.updateLUCharges(
                tripId = params.tripId,
                loadingFile = params.loadingFile,
                unloadingFile = params.unloadingFile,
                request = params.request,
            )
        }
    }
