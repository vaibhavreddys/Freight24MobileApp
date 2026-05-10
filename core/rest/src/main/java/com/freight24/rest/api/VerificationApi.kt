package com.freight24.rest.api

import com.freight24.rest.request.UpdateVerificationRequestStatusRequest
import com.freight24.rest.response.BaseResponse
import com.freight24.rest.response.VerificationClientsResponse
import com.freight24.rest.response.VerificationTripsResponse
import com.freight24.rest.response.VerificationTruckOwnersResponse
import com.freight24.rest.response.VerificationTrucksResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface VerificationApi {
    @GET("verifications/trips")
    suspend fun getVerificationTrips(
        @Query("status") status: String? = null,
    ): Response<VerificationTripsResponse>

    @GET("verifications/trucks")
    suspend fun getVerificationTrucks(
        @Query("status") status: String? = null,
    ): Response<VerificationTrucksResponse>

    @GET("verifications/truck-owners")
    suspend fun getVerificationTruckOwners(
        @Query("status") status: String? = null,
    ): Response<VerificationTruckOwnersResponse>

    @GET("verifications/clients")
    suspend fun getVerificationClients(
        @Query("status") status: String? = null,
    ): Response<VerificationClientsResponse>

    @PATCH("verifications/trips/{id}")
    suspend fun updateVerificationRequestStatus(
        @Path("id") id: String,
        @Body request: UpdateVerificationRequestStatusRequest,
    ): Response<BaseResponse>

    @PATCH("verifications/trucks/{id}")
    suspend fun updateTruckVerificationStatus(
        @Path("id") id: String,
        @Body request: UpdateVerificationRequestStatusRequest,
    ): Response<BaseResponse>

    @PATCH("verifications/truck-owners/{id}")
    suspend fun updateTruckOwnerVerificationStatus(
        @Path("id") id: String,
        @Body request: UpdateVerificationRequestStatusRequest,
    ): Response<BaseResponse>

    @PATCH("verifications/clients/{id}")
    suspend fun updateClientVerificationStatus(
        @Path("id") id: String,
        @Body request: UpdateVerificationRequestStatusRequest,
    ): Response<BaseResponse>
}
