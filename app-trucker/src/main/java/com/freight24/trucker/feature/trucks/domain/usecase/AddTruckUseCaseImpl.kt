package com.freight24.trucker.feature.trucks.domain.usecase

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
            val userId = authDataStore.userId.first() ?: return Result.failure(Exception("User not authenticated"))

            val registrationNumber = params.registrationNumber.toPart("registration_number")
            val truckTypeId = params.truckTypeId.toString().toPart("truck_type_id")
            val truckOwnerId = userId.toPart("truck_owner_id")
            val make = params.make?.toPart("make")
            val model = params.model?.toPart("model")
            val year = params.year?.toPart("year")

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
                rcCard,
                fcCertificate,
                insurance,
            )
        }

        private fun String.toPart(name: String): MultipartBody.Part {
            return MultipartBody.Part.createFormData(name, this)
        }

        private fun java.io.File.toFilePart(name: String): MultipartBody.Part {
            val requestBody = this.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            return MultipartBody.Part.createFormData(name, this.name, requestBody)
        }
    }
