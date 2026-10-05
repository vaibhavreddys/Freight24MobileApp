package com.freight24.rest.api

import com.freight24.rest.request.CancelTripRequest
import com.freight24.rest.request.CreateTripRequest
import com.freight24.rest.request.LrVerifyRequest
import com.freight24.rest.request.UpdateOtherDeductionsRequest
import com.freight24.rest.request.UpdateTripRequest
import com.freight24.rest.request.UpdateTruckerStatus
import com.freight24.rest.response.BaseResponse
import com.freight24.rest.response.CreatedTripResponse
import com.freight24.rest.response.MyTripsDataResponse
import com.freight24.rest.response.PaymentTransactionsResponse
import com.freight24.rest.response.PodUploadResponse
import com.freight24.rest.response.SignedDocument
import com.freight24.rest.response.TripDetailDataResponse
import com.freight24.rest.response.TripDetailResponse
import com.freight24.rest.response.TripResponse
import com.freight24.rest.response.TripSearchResponse
import com.freight24.rest.response.UpdateOtherDeductionsResponse
import com.freight24.rest.response.UpdateTripResponse
import com.freight24.rest.response.UploadedDataResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
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
        @Body request: UpdateTruckerStatus,
    ): Response<BaseResponse>

    @POST("trips")
    suspend fun createTrip(
        @Body createTripRequest: CreateTripRequest,
    ): Response<CreatedTripResponse>

    @GET("employee/trips/my-trips")
    suspend fun getEmpMyTrips(
        @Query("filter") filter: String?,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 200,
    ): Response<MyTripsDataResponse>

    @GET("employee/trips/search")
    suspend fun searchTrips(
        @Query("q") query: String,
        @Query("scope") scope: String = "my",
    ): Response<TripSearchResponse>

    @GET("employee/trips/{tripId}")
    suspend fun getEmpTripDetails(
        @Path("tripId") tripId: String,
    ): Response<TripDetailDataResponse>

    @GET("trips/{tripId}/payment-summary")
    suspend fun getPaymentTransactions(
        @Path("tripId") tripId: String,
        @Query("userType") userType: String,
        @Query("view") view: String = "transactions",
    ): Response<PaymentTransactionsResponse>

    @Multipart
    @POST("trips/{tripId}/lr")
    suspend fun uploadLr(
        @Path("tripId") tripId: String,
        @Part file: MultipartBody.Part,
        @Part("notes") description: RequestBody? = null,
    ): Response<UploadedDataResponse>

    @Multipart
    @POST("trips/{trip_id}/pod")
    suspend fun uploadPod(
        @Path("trip_id") tripId: String,
        @Part file: MultipartBody.Part,
        @Part("pod_type") podType: RequestBody,
        @Part("notes") notes: RequestBody? = null,
    ): Response<PodUploadResponse>

    @Multipart
    @PUT("trips/{trip_id}/pod/{pod_id}")
    suspend fun replacePod(
        @Path("trip_id") tripId: String,
        @Path("pod_id") podId: String,
        @Part file: MultipartBody.Part,
        @Part("notes") notes: RequestBody? = null,
    ): Response<PodUploadResponse>

    @GET("trips/{trip_id}/documents/{document_id}/signed-url")
    suspend fun getSignedDocument(
        @Path("trip_id") tripId: String,
        @Path("document_id") documentId: String,
    ): Response<SignedDocument>

    @PATCH("trips/{tripId}/lr/verify")
    suspend fun verifyLr(
        @Path("tripId") tripId: String,
        @Body lrVerifyRequest: LrVerifyRequest,
    ): Response<TripDetailDataResponse>

    @PATCH("trips/{trip_id}/other-deductions")
    suspend fun updateOtherDeductions(
        @Path("trip_id") tripId: String,
        @Body updateOtherDeductionsRequest: UpdateOtherDeductionsRequest,
    ): Response<UpdateOtherDeductionsResponse>

    @PATCH("trips/{trip_id}/edit-basic")
    suspend fun updateTrip(
        @Path("trip_id") tripId: String,
        @Body updateTripRequest: UpdateTripRequest,
    ): Response<UpdateTripResponse>

    @POST("trips/{trip_id}/cancel")
    suspend fun cancelTrip(
        @Path("trip_id") tripId: String,
        @Body cancelTripRequest: CancelTripRequest,
    ): Response<BaseResponse>
}
