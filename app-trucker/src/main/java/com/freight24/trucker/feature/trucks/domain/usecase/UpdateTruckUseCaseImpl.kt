package com.freight24.trucker.feature.trucks.domain.usecase

import com.freight24.rest.response.AddTruckResponse
import com.freight24.trucker.feature.trucks.data.repository.TruckRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject

internal class UpdateTruckUseCaseImpl @Inject constructor(
    private val repository: TruckRepository
) : UpdateTruckUseCase {
    override suspend fun invoke(params: UpdateTruckParams): Result<AddTruckResponse> {
        val registrationNumber = params.registrationNumber.toPart("registration_number")
        val truckTypeId = params.truckTypeId.toString().toPart("truck_type_id")
        val make = params.make?.toPart("make")
        val model = params.model?.toPart("model")
        val year = params.year?.toPart("year")

        val rcCard = params.rcCard?.toFilePart("rc_card")
        val fcCertificate = params.fcCertificate?.toFilePart("fc_certificate")
        val insurance = params.insurance?.toFilePart("insurance")

        return repository.updateTruck(
            params.truckId,
            registrationNumber,
            truckTypeId,
            make,
            model,
            year,
            rcCard,
            fcCertificate,
            insurance
        )
    }

    private fun String.toPart(name: String): MultipartBody.Part {
        return MultipartBody.Part.createFormData(name, this)
    }

    private fun java.io.File.toFilePart(name: String): MultipartBody.Part {
        val mimeType = if (extension.equals("pdf", ignoreCase = true)) "application/pdf" else "application/octet-stream"
        val requestBody = this.asRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(name, this.name, requestBody)
    }
}
