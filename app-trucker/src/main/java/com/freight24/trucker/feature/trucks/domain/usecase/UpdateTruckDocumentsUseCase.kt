package com.freight24.trucker.feature.trucks.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.rest.response.AddTruckResponse
import okhttp3.MultipartBody

data class UpdateTruckDocumentsParams(
    val truckId: String,
    val rcCard: MultipartBody.Part? = null,
    val fcCertificate: MultipartBody.Part? = null,
    val insurance: MultipartBody.Part? = null,
) : UseCaseParams

interface UpdateTruckDocumentsUseCase : UseCase<UpdateTruckDocumentsParams, AddTruckResponse>
