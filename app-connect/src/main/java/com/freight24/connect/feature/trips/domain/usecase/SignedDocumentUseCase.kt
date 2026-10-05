package com.freight24.connect.feature.trips.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.trips.TripRepository
import com.freight24.rest.response.SignedDocument
import javax.inject.Inject

data class SignedDocumentUseCaseDataParams(
    val tripId: String,
    val documentId: String,
) : UseCaseParams

internal interface SignedDocumentUseCase : UseCase<SignedDocumentUseCaseDataParams, SignedDocument>

class SignedDocumentUseCaseImpl
    @Inject
    constructor(
        private val repository: TripRepository,
    ) : SignedDocumentUseCase {
        override suspend fun invoke(params: SignedDocumentUseCaseDataParams): Result<SignedDocument> {
            return repository.getSignedDocument(
                params.tripId,
                params.documentId,
            )
        }
    }
