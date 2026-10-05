package com.freight24.connect.feature.verificationrequests.domain.usecase

import com.freight24.connect.feature.verificationrequests.domain.model.BusinessProfileChange
import com.freight24.connect.feature.verificationrequests.domain.model.ClientVerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.TruckOwnerVerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestStatus
import com.freight24.connect.repository.verificationrequests.VerificationRequestsRepository
import com.freight24.rest.response.VerificationClientDto
import com.freight24.rest.response.VerificationTruckOwnerDto
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

interface GetVerificationTruckOwnerRequestsUseCase {
    suspend operator fun invoke(status: VerificationRequestStatus? = null): Result<List<TruckOwnerVerificationRequest>>
}

class GetVerificationTruckOwnerRequestsUseCaseImpl
    @Inject
    constructor(
        private val repository: VerificationRequestsRepository,
    ) : GetVerificationTruckOwnerRequestsUseCase {
        override suspend fun invoke(status: VerificationRequestStatus?): Result<List<TruckOwnerVerificationRequest>> {
            return repository.getVerificationTruckOwners(status?.apiValue).map { response ->
                response.data.map { it.toDomain() }
            }
        }

        private fun VerificationTruckOwnerDto.toDomain(): TruckOwnerVerificationRequest {
            val changedKeys = changedFields?.businessProfile.orEmpty().filter { it != "_ref" }
            return TruckOwnerVerificationRequest(
                id = id,
                referenceId = referenceId,
                fullName = fullName.orEmpty().ifBlank { "Truck Owner" },
                phone = phone,
                changedFields = changedKeys,
                changedBusinessProfile =
                    buildBusinessProfileChanges(
                        changedKeys = changedKeys,
                        oldProfile = oldSnapshot?.businessProfile.orEmpty(),
                        newProfile = newSnapshot?.businessProfile.orEmpty(),
                    ),
                status = VerificationRequestStatus.fromApiValue(status) ?: VerificationRequestStatus.PENDING,
                resolutionNotes = resolutionNotes,
                updatedAt = updatedAt,
            )
        }
    }

interface GetVerificationClientRequestsUseCase {
    suspend operator fun invoke(status: VerificationRequestStatus? = null): Result<List<ClientVerificationRequest>>
}

class GetVerificationClientRequestsUseCaseImpl
    @Inject
    constructor(
        private val repository: VerificationRequestsRepository,
    ) : GetVerificationClientRequestsUseCase {
        override suspend fun invoke(status: VerificationRequestStatus?): Result<List<ClientVerificationRequest>> {
            return repository.getVerificationClients(status?.apiValue).map { response ->
                response.data.map { it.toDomain() }
            }
        }

        private fun VerificationClientDto.toDomain(): ClientVerificationRequest {
            val changedKeys = changedFields?.businessProfile.orEmpty().filter { it != "_ref" }
            return ClientVerificationRequest(
                id = id,
                referenceId = referenceId,
                fullName = fullName.orEmpty().ifBlank { "Client" },
                companyName = companyName.orEmpty().ifBlank { "Client Company" },
                changedFields = changedKeys,
                changedBusinessProfile =
                    buildBusinessProfileChanges(
                        changedKeys = changedKeys,
                        oldProfile = oldSnapshot?.businessProfile.orEmpty(),
                        newProfile = newSnapshot?.businessProfile.orEmpty(),
                    ),
                status = VerificationRequestStatus.fromApiValue(status) ?: VerificationRequestStatus.PENDING,
                resolutionNotes = resolutionNotes,
                updatedAt = updatedAt,
            )
        }
    }

private fun buildBusinessProfileChanges(
    changedKeys: List<String>,
    oldProfile: Map<String, JsonElement?>,
    newProfile: Map<String, JsonElement?>,
): List<BusinessProfileChange> {
    return changedKeys.mapNotNull { key ->
        val beforeValue = extractProfileValue(oldProfile, key)
        val afterValue = extractProfileValue(newProfile, key)

        if (beforeValue == null && afterValue == null) return@mapNotNull null

        BusinessProfileChange(
            fieldName = key,
            displayFieldName = key,
            beforeValue = beforeValue,
            afterValue = afterValue,
        )
    }
}

private fun extractProfileValue(
    snapshot: Map<String, JsonElement?>,
    key: String,
): String? {
    val element = snapshot[key] ?: return null
    return when (element) {
        is JsonPrimitive -> element.content
        is JsonObject -> element.toString()
        else -> element.toString()
    }
}
