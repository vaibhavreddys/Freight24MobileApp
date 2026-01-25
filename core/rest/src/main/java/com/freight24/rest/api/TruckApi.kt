package com.freight24.rest.api

import com.freight24.rest.response.AddTruckResponse
import com.freight24.rest.response.TruckDetailResponse
import com.freight24.rest.response.TruckResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface TruckApi {
    @GET("trucks")
    suspend fun getTrucks(): Response<TruckResponse>

    @GET("trucks/{id}")
    suspend fun getTruckDetail(
        @Path("id") id: String
    ): Response<TruckDetailResponse>

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
        @Part insurance: MultipartBody.Part?
    ): Response<AddTruckResponse>
}
