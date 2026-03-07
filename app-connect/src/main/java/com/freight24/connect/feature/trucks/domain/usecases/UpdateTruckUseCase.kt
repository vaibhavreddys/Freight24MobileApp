package com.freight24.connect.feature.trucks.domain.usecases

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.TruckRepository
import com.freight24.rest.response.AddTruckResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

data class UpdateTruckParams(
    val truckId: String,
    val registrationNumber: String,
    val truckTypeId: Int,
    val make: String? = null,
    val model: String? = null,
    val year: String? = null,
    val rcCard: File? = null,
    val fcCertificate: File? = null,
    val insurance: File? = null,
) : UseCaseParams

interface UpdateTruckUseCase : UseCase<UpdateTruckParams, AddTruckResponse>

class UpdateTruckUseCaseImpl @Inject constructor(
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
            id = params.truckId,
            registrationNumber = registrationNumber,
            truckTypeId = truckTypeId,
            make = make,
            model = model,
            year = year,
            rcCard = rcCard,
            fcCertificate = fcCertificate,
            insurance = insurance
        )
    }

    private fun String.toPart(name: String): MultipartBody.Part {
        return MultipartBody.Part.createFormData(name, this)
    }

    private fun File.toFilePart(name: String): MultipartBody.Part {
        val requestBody = this.asRequestBody("application/octet-stream".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(name, this.name, requestBody)
    }
}
