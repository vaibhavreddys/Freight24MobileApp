package com.freight24.trucker.feature.trips.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.trucker.feature.trips.data.repository.TripRepository
import javax.inject.Inject

data class GetSignedDocumentParams(
    val tripId: String,
    val documentId: String
) : UseCaseParams

internal interface GetSignedDocumentUseCase : UseCase<GetSignedDocumentParams, String>

internal class GetSignedDocumentUseCaseImpl @Inject constructor(
    private val repository: TripRepository
) : GetSignedDocumentUseCase {
    override suspend fun invoke(params: GetSignedDocumentParams): Result<String> {
        return repository.getSignedDocument(params.tripId, params.documentId)
    }
}
