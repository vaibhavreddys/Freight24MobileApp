package com.freight24.connect.feature.trucks.domain.usecases

import com.freight24.connect.repository.TruckRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

data class AddTruckParams(
    val registrationNumber: String,
    val truckTypeId: Int,
    val ownerProfileId: String,
    val make: String? = null,
    val model: String? = null,
    val year: String? = null,
    val capacityMt: String? = null,
    val rcCard: File? = null,
    val fcCertificate: File? = null,
    val insurance: File? = null,
)

data class CreatedTruck(
    val truckId: String,
    val registrationNumber: String,
    val truckType: String?,
    val status: String,
)

interface AddTruckUseCase {
    suspend operator fun invoke(params: AddTruckParams): Result<CreatedTruck>
}

class AddTruckUseCaseImpl
    @Inject
    constructor(
        private val repository: TruckRepository,
    ) : AddTruckUseCase {
        override suspend fun invoke(params: AddTruckParams): Result<CreatedTruck> {
            val registrationNumber = params.registrationNumber.toPart("registration_number")
            val truckTypeId = params.truckTypeId.toString().toPart("truck_type_id")
            val ownerProfileId = params.ownerProfileId.toPart("truck_owner_id")
            val make = params.make?.toPart("make")
            val model = params.model?.toPart("model")
            val year = params.year?.toPart("year")
            val capacityMt = params.capacityMt?.toPart("capacity_mt")

            val rcCard = params.rcCard?.toFilePart("rc_card")
            val fcCertificate = params.fcCertificate?.toFilePart("fc_certificate")
            val insurance = params.insurance?.toFilePart("insurance")

            return repository
                .addTruck(
                    registrationNumber,
                    truckTypeId,
                    ownerProfileId,
                    make,
                    model,
                    year,
                    capacityMt,
                    rcCard,
                    fcCertificate,
                    insurance,
                ).mapCatching { response ->
                    if (!response.success) {
                        throw IllegalStateException(response.message.ifBlank { "Failed to create truck" })
                    }
                    val truck = response.data
                        ?: throw IllegalStateException(response.message.ifBlank { "Truck creation returned no data" })

                    CreatedTruck(
                        truckId = truck.truckId,
                        registrationNumber = truck.registrationNumber,
                        truckType = truck.displayName ?: truck.truckType,
                        status = truck.status,
                    )
                }
        }

        private fun String.toPart(name: String): MultipartBody.Part {
            return MultipartBody.Part.createFormData(name, this)
        }

        private fun File.toFilePart(name: String): MultipartBody.Part {
            val mimeType = if (extension.equals("pdf", ignoreCase = true)) "application/pdf" else "application/octet-stream"
            val requestBody = this.asRequestBody(mimeType.toMediaTypeOrNull())
            return MultipartBody.Part.createFormData(name, this.name, requestBody)
        }
    }
