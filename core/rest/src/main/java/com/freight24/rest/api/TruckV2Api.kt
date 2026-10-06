package com.freight24.rest.api

import com.freight24.rest.response.CreateTruckV2Response
import com.freight24.rest.response.TruckV2DetailResponse
import com.freight24.rest.response.TruckV2PageResponse
import com.freight24.rest.response.TruckV2ValidationResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query

interface TruckV2Api {
    @GET("/api/v2/trucks")
    suspend fun getTrucks(
        @Query("status") status: String? = null,
        @Query("document_status") documentStatus: String? = null,
        @Query("trucker_id") ownerProfileId: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 30,
    ): Response<TruckV2PageResponse>

    @GET("/api/v2/trucks/search")
    suspend fun searchTrucks(
        @Query("q") query: String,
        @Query("status") status: String? = null,
        @Query("document_status") documentStatus: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 30,
    ): Response<TruckV2PageResponse>

    @GET("/api/v2/trucks/{truckId}")
    suspend fun getTruckDetail(
        @Path("truckId") truckId: String,
    ): Response<TruckV2DetailResponse>

    @GET("/api/v2/trucks/validate")
    suspend fun validateRegistration(
        @Query("registration_number") registrationNumber: String,
        @Query("excludeTruckId") excludeTruckId: String? = null,
    ): Response<TruckV2ValidationResponse>

    @Multipart
    @POST("/api/v2/trucks")
    suspend fun createTruck(
        @Part registration_number: MultipartBody.Part,
        @Part truck_type_id: MultipartBody.Part,
        @Part truck_owner_id: MultipartBody.Part,
        @Part make: MultipartBody.Part?,
        @Part model: MultipartBody.Part?,
        @Part year: MultipartBody.Part?,
        @Part capacity_mt: MultipartBody.Part?,
        @Part rc_card: MultipartBody.Part?,
        @Part fc_certificate: MultipartBody.Part?,
        @Part insurance: MultipartBody.Part?,
    ): Response<CreateTruckV2Response>

    @Multipart
    @PUT("/api/v2/trucks/{truckId}")
    suspend fun updateTruck(
        @Path("truckId") truckId: String,
        @Part registrationNumber: MultipartBody.Part,
        @Part truckTypeId: MultipartBody.Part,
        @Part make: MultipartBody.Part?,
        @Part model: MultipartBody.Part?,
        @Part year: MultipartBody.Part?,
        @Part capacityMt: MultipartBody.Part?,
        @Part rcCard: MultipartBody.Part?,
        @Part fcCertificate: MultipartBody.Part?,
        @Part insurance: MultipartBody.Part?,
    ): Response<TruckV2DetailResponse>
}
