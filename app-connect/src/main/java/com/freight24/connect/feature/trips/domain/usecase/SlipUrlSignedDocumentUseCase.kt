package com.freight24.connect.feature.trips.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.trips.TripLUChargesRepository
import com.freight24.rest.response.SignedDocument
import javax.inject.Inject

data class SlipUrlSignedDocumentUseCaseDataParams(
    val tripId : String,
    val chargeId: String,
): UseCaseParams

internal interface SlipUrlSignedDocumentUseCase : UseCase<SlipUrlSignedDocumentUseCaseDataParams, SignedDocument>

class SlipUrlSignedDocumentUseCaseImpl @Inject constructor(
    private val repository: TripLUChargesRepository
) : SlipUrlSignedDocumentUseCase {

    override suspend fun invoke(
        params: SlipUrlSignedDocumentUseCaseDataParams
    ): Result<SignedDocument> {
        return repository.getSignedLUDocument(
            params.tripId,
            params.chargeId
        )
    }
}