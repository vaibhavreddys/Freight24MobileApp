package com.freight24.connect.repository

import com.freight24.rest.response.AddTruckResponse
import com.freight24.rest.response.SearchTruckResponse
import com.freight24.rest.response.TruckDetailResponse
import com.freight24.rest.response.TruckDto
import okhttp3.MultipartBody

interface TruckRepository {
    suspend fun searchTrucks(query: String): Result<List<SearchTruckResponse.Truck>>

    suspend fun getTrucks(): Result<List<TruckDto>>

    suspend fun getTruckDetail(id: String): Result<TruckDetailResponse>

    suspend fun getTruckDocumentSignedUrl(
        truckId: String,
        documentType: String,
    ): Result<String>

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

    suspend fun checkTruckAvailability(registrationNumber: String): Result<String?>
}
