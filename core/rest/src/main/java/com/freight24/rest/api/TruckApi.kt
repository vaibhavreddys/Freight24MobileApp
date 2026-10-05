package com.freight24.rest.api

import com.freight24.rest.response.AddTruckResponse
import com.freight24.rest.response.SearchTruckResponse
import com.freight24.rest.response.TruckAvailabilityResponse
import com.freight24.rest.response.TruckDetailResponse
import com.freight24.rest.response.TruckResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface TruckApi {
    @GET("trucks")
    suspend fun getTrucks(): Response<TruckResponse>

    @GET("trucks/{id}")
    suspend fun getTruckDetail(
        @Path("id") id: String,
    ): Response<TruckDetailResponse>

    @GET("trucks/check-availability")
    suspend fun checkTruckAvailability(
        @Query("registration_number") registrationNumber: String,
    ): Response<TruckAvailabilityResponse>

    @Multipart
    @POST("trucks")
    suspend fun addTruck(
        @Part registration_number: MultipartBody.Part,
        @Part truck_type_id: MultipartBody.Part,
        @Part truck_owner_id: MultipartBody.Part,
        @Part make: MultipartBody.Part?,
        @Part model: MultipartBody.Part?,
        @Part year: MultipartBody.Part?,
        @Part rc_card: MultipartBody.Part?,
        @Part fc_certificate: MultipartBody.Part?,
        @Part insurance: MultipartBody.Part?,
    ): Response<AddTruckResponse>

    @Multipart
    @retrofit2.http.PUT("trucks/{id}")
    suspend fun updateTruck(
        @Path("id") id: String,
        @Part registration_number: MultipartBody.Part? = null,
        @Part truck_type_id: MultipartBody.Part? = null,
        @Part make: MultipartBody.Part? = null,
        @Part model: MultipartBody.Part? = null,
        @Part year: MultipartBody.Part? = null,
        @Part rc_card: MultipartBody.Part? = null,
        @Part fc_certificate: MultipartBody.Part? = null,
        @Part insurance: MultipartBody.Part? = null,
    ): Response<AddTruckResponse>

    @GET("trucks/search")
    suspend fun searchTrucks(
        @Query("q") query: String,
    ): Response<SearchTruckResponse>
}
