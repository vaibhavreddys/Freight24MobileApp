package com.freight24.rest.api

import com.freight24.rest.request.CreateTripRequest
import com.freight24.rest.request.UpdateTruckerStatus
import com.freight24.rest.response.CreatedTripResponse
import com.freight24.rest.response.TripDetailResponse
import com.freight24.rest.response.MyTripsDataResponse
import com.freight24.rest.response.SignedDocument
import com.freight24.rest.response.TripDetailDataResponse
import com.freight24.rest.response.TripResponse
import com.freight24.rest.response.UploadedDataResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface TripApi {
    @GET("trips/trucker/list")
    suspend fun getTrips(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20,
    ): Response<TripResponse>

    @GET("trips/trucker/trip/{tripId}")
    suspend fun getTripDetail(
        @Path("tripId") tripId: String,
    ): Response<TripDetailResponse>

    @PATCH("trips/{tripId}/trucker-status")
    suspend fun updateTripStatus(
        @Path("tripId") tripId: String,
        @Body request: com.freight24.rest.request.UpdateTripStatusRequest,
    ): Response<TripDetailResponse>

    @POST("trips")
    suspend fun createTrip(
        @Body createTripRequest: CreateTripRequest
    ): Response<CreatedTripResponse>

    @GET("employee/trips/my-trips")
    suspend fun getEmpMyTrips(
        @Query("filter") filter: String?,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 200,
    ): Response<MyTripsDataResponse>

    @GET("employee/trips/{tripId}")
    suspend fun getEmpTripDetails(
        @Path("tripId") tripId: String,
    ): Response<TripDetailDataResponse>

    @Multipart
    @POST("trips/{tripId}/lr")
    suspend fun uploadLr(
        @Path("tripId") tripId: String,
        @Part file: MultipartBody.Part,
        @Part("notes") description: RequestBody? = null
    ): Response<UploadedDataResponse>

    @PATCH("trips/{tripId}/trucker-status")
    suspend fun updateTruckerStatus(
        @Path("tripId") tripId: String,
        @Body request: UpdateTruckerStatus
    ): Response<TripDetailDataResponse>

    @GET("trips/{trip_id}/documents/{document_id}/signed-url")
    suspend fun getSignedDocument(
        @Path("trip_id") tripId: String,
        @Path("document_id") documentId: String,
    ): Response<SignedDocument>

    @PATCH("trips/{tripId}/documents/{documentId}/verify")
    suspend fun verifyLr(
        @Path("tripId") tripId: String,
        @Path("documentId") documentId: String
    ): Response<TripDetailDataResponse>

}