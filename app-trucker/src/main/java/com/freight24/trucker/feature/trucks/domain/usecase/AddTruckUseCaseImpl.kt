package com.freight24.trucker.feature.trucks.domain.usecase

import com.freight24.common.util.DocumentUploadException
import com.freight24.common.util.DocumentUploadPolicy
import com.freight24.auth.AuthDataStore
import com.freight24.rest.response.AddTruckResponse
import com.freight24.trucker.feature.trucks.data.repository.TruckRepository
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject

internal class AddTruckUseCaseImpl
    @Inject
    constructor(
        private val repository: TruckRepository,
        private val authDataStore: AuthDataStore,
    ) : AddTruckUseCase {
        override suspend fun invoke(params: AddTruckParams): Result<AddTruckResponse> {
            try {
                val userId = authDataStore.userId.first() ?: return Result.failure(Exception("User not authenticated"))

                val registrationNumber = params.registrationNumber.toPart("registration_number")
                val truckTypeId = params.truckTypeId.toString().toPart("truck_type_id")
                val truckOwnerId = userId.toPart("truck_owner_id")
                val make = params.make?.toPart("make")
                val model = params.model?.toPart("model")
                val year = params.year?.toPart("year")
                val capacityMt = params.capacityMt.toPart("capacity_mt")

                val rcCard = params.rcCard?.toFilePart("rc_card")
                val fcCertificate = params.fcCertificate?.toFilePart("fc_certificate")
                val insurance = params.insurance?.toFilePart("insurance")

                return repository.addTruck(
                    registrationNumber,
                    truckTypeId,
                    truckOwnerId,
                    make,
                    model,
                    year,
                    capacityMt,
                    rcCard,
                    fcCertificate,
                    insurance,
                )
            } catch (error: DocumentUploadException) {
                return Result.failure(error)
            }
        }

        private fun String.toPart(name: String): MultipartBody.Part {
            return MultipartBody.Part.createFormData(name, this)
        }

        private fun java.io.File.toFilePart(name: String): MultipartBody.Part {
            val mimeType = if (extension.equals("pdf", ignoreCase = true)) "application/pdf" else "application/octet-stream"
            DocumentUploadPolicy.validateFile(this)
            val requestBody = this.asRequestBody(mimeType.toMediaTypeOrNull())
            return MultipartBody.Part.createFormData(name, this.name, requestBody)
        }
    }
