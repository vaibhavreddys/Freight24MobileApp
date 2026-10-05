package com.freight24.trucker.feature.trucks.data.repository

import com.freight24.rest.response.AddTruckResponse
import com.freight24.rest.response.TruckDetailResponse
import com.freight24.rest.response.TruckResponse
import okhttp3.MultipartBody

interface TruckRepository {
    suspend fun fetchTrucks(): Result<TruckResponse>

    suspend fun fetchTruckDetail(id: String): Result<TruckDetailResponse>

    suspend fun addTruck(
        registrationNumber: MultipartBody.Part,
        truckTypeId: MultipartBody.Part,
        truckOwnerId: MultipartBody.Part,
        make: MultipartBody.Part?,
        model: MultipartBody.Part?,
        year: MultipartBody.Part?,
        rcCard: MultipartBody.Part?,
        fcCertificate: MultipartBody.Part?,
        insurance: MultipartBody.Part?,
    ): Result<AddTruckResponse>

    suspend fun updateTruckDocuments(
        truckId: String,
        rcCard: MultipartBody.Part? = null,
        fcCertificate: MultipartBody.Part? = null,
        insurance: MultipartBody.Part? = null,
    ): Result<AddTruckResponse>

    suspend fun updateTruck(
        id: String,
        registrationNumber: MultipartBody.Part,
        truckTypeId: MultipartBody.Part,
        make: MultipartBody.Part?,
        model: MultipartBody.Part?,
        year: MultipartBody.Part?,
        rcCard: MultipartBody.Part? = null,
        fcCertificate: MultipartBody.Part? = null,
        insurance: MultipartBody.Part? = null,
    ): Result<AddTruckResponse>

    suspend fun getTruckDocumentSignedUrl(
        truckId: String,
        documentType: String,
    ): Result<String>

    suspend fun checkTruckAvailability(registrationNumber: String): Result<String?>
}
