package com.freight24.connect.feature.verificationrequests.domain.usecase

import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestDocument
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestLocation
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestStatus
import com.freight24.connect.repository.verificationrequests.VerificationRequestsRepository
import com.freight24.rest.response.VerificationTripDto
import javax.inject.Inject

interface GetVerificationRequestsUseCase {
    suspend operator fun invoke(status: VerificationRequestStatus? = null): Result<List<VerificationRequest>>
}

class GetVerificationRequestsUseCaseImpl
    @Inject
    constructor(
        private val repository: VerificationRequestsRepository,
    ) : GetVerificationRequestsUseCase {
        override suspend fun invoke(status: VerificationRequestStatus?): Result<List<VerificationRequest>> {
            return repository.getVerificationTrips(status?.apiValue).map { response ->
                response.data.map { it.toDomain() }
            }
        }

        private fun VerificationTripDto.toDomain(): VerificationRequest {
            val snapshotDocuments = newSnapshot?.documents.orEmpty()
            val changedDocumentKeys = changedFields?.documents.orEmpty()

            val orderedDocuments =
                buildList {
                    if (changedDocumentKeys.isNotEmpty()) {
                        changedDocumentKeys.forEach { key ->
                            add(
                                VerificationRequestDocument(
                                    name = key,
                                    entity = "trip",
                                    requestId = id,
                                    s3Url = snapshotDocuments[key]?.fileUrl,
                                ),
                            )
                        }
                    }

                    snapshotDocuments.forEach { (key, document) ->
                        if (none { it.name.equals(key, ignoreCase = true) }) {
                            add(
                                VerificationRequestDocument(
                                    name = key,
                                    entity = "trip",
                                    requestId = id,
                                    s3Url = document.fileUrl,
                                ),
                            )
                        }
                    }
                }

            return VerificationRequest(
                id = id,
                tripId = tripId,
                tripCode = tripCode,
                truckRegistrationNumber = truckRegistrationNumber,
                origin = origin?.toDomain(),
                destination = destination?.toDomain(),
                documents = orderedDocuments,
                status = VerificationRequestStatus.fromApiValue(status) ?: VerificationRequestStatus.PENDING,
                resolutionNotes = resolutionNotes,
                updatedAt = updatedAt,
            )
        }

        private fun com.freight24.rest.response.VerificationLocationDto.toDomain(): VerificationRequestLocation {
            return VerificationRequestLocation(
                address = address,
                city = city,
                area = area,
            )
        }
    }
