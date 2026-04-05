package com.freight24.connect.feature.verificationrequests.domain.usecase

import com.freight24.connect.feature.verificationrequests.domain.model.TruckDetailChange
import com.freight24.connect.feature.verificationrequests.domain.model.TruckVerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestDocument
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestStatus
import com.freight24.connect.repository.verificationrequests.VerificationRequestsRepository
import com.freight24.rest.response.VerificationTruckDto
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

interface GetVerificationTruckRequestsUseCase {
    suspend operator fun invoke(status: VerificationRequestStatus? = null): Result<List<TruckVerificationRequest>>
}

class GetVerificationTruckRequestsUseCaseImpl @Inject constructor(
    private val repository: VerificationRequestsRepository,
) : GetVerificationTruckRequestsUseCase {
    override suspend fun invoke(status: VerificationRequestStatus?): Result<List<TruckVerificationRequest>> {
        return repository.getVerificationTrucks(status?.apiValue).map { response ->
            response.data.map { it.toDomain() }
        }
    }

    private fun VerificationTruckDto.toDomain(): TruckVerificationRequest {
        val changedDocumentKeys = changedFields?.documents.orEmpty()
        val snapshotDocuments = newSnapshot?.documents.orEmpty()

        val orderedDocuments = buildList {
            changedDocumentKeys.forEach { key ->
                add(
                    VerificationRequestDocument(
                        name = key,
                        s3Url = snapshotDocuments[key]?.fileUrl,
                    ),
                )
            }
        }

        val changedDetailKeys = changedFields?.truckDetails.orEmpty().filter { it != "_ref" }
        val oldDetails = oldSnapshot?.truckDetails.orEmpty()
        val newDetails = newSnapshot?.truckDetails.orEmpty()

        val displayKeyOverrides = mapOf(
            "truck_type_id" to "truck_type_name",
        )

        val truckDetailChanges = changedDetailKeys.mapNotNull { key ->
            val displayKey = displayKeyOverrides[key]
            val effectiveKey = if (displayKey != null && newDetails.containsKey(displayKey)) {
                displayKey
            } else {
                key
            }

            val beforeValue = extractValue(oldDetails, effectiveKey, oldDetails[key])
            val afterValue = extractValue(newDetails, effectiveKey, newDetails[key])

            if (beforeValue == null && afterValue == null) return@mapNotNull null

            TruckDetailChange(
                fieldName = key,
                displayFieldName = effectiveKey,
                beforeValue = beforeValue,
                afterValue = afterValue,
            )
        }

        return TruckVerificationRequest(
            id = id,
            referenceId = referenceId,
            registrationNumber = registrationNumber,
            changedFields = changedDocumentKeys + changedDetailKeys,
            changedDocuments = orderedDocuments,
            changedTruckDetails = truckDetailChanges,
            status = VerificationRequestStatus.fromApiValue(status) ?: VerificationRequestStatus.PENDING,
            resolutionNotes = resolutionNotes,
            updatedAt = updatedAt,
        )
    }

    private fun extractValue(
        snapshot: Map<String, JsonElement?>,
        key: String,
        fallbackElement: JsonElement? = null,
    ): String? {
        val element = snapshot[key] ?: fallbackElement ?: return null
        return when (element) {
            is JsonPrimitive -> element.content
            is JsonObject -> element.toString()
            else -> element.toString()
        }
    }
}
