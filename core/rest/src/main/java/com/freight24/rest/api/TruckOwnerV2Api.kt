package com.freight24.rest.api

import com.freight24.rest.response.TruckOwnerOnboardingV2Response
import com.freight24.rest.request.UpdateTruckOwnerStatusRequest
import com.freight24.rest.response.TruckOwnerV2DetailResponse
import com.freight24.rest.response.TruckOwnerV2PageResponse
import com.freight24.rest.response.TruckOwnerV2ValidationResponse
import okhttp3.MultipartBody
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

interface TruckOwnerV2Api {
    @GET("/api/v2/truck-owners")
    suspend fun getTruckOwners(
        @Query("status") status: String? = null,
        @Query("created_by") createdBy: String? = null,
        @Query("document_status") documentStatus: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 30,
    ): Response<TruckOwnerV2PageResponse>

    @GET("/api/v2/truck-owners/search")
    suspend fun searchTruckOwners(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 30,
    ): Response<TruckOwnerV2PageResponse>

    @GET("/api/v2/truck-owners/basic/search")
    suspend fun searchTruckOwnerOptions(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 30,
    ): Response<TruckOwnerV2PageResponse>

    @GET("/api/v2/truck-owners/{userId}")
    suspend fun getTruckOwnerDetail(
        @Path("userId") ownerUserId: String,
    ): Response<TruckOwnerV2DetailResponse>

    @GET("/api/v2/truck-owners/validate")
    suspend fun validateTruckOwner(
        @Query("phone") phone: String? = null,
        @Query("pan") pan: String? = null,
        @Query("aadhar") aadhaar: String? = null,
        @Query("gst") gst: String? = null,
        @Query("excludeUserId") excludeOwnerUserId: String? = null,
    ): Response<TruckOwnerV2ValidationResponse>

    @PATCH("/api/v2/truck-owners/{userId}/status")
    suspend fun updateTruckOwnerStatus(
        @Path("userId") ownerUserId: String,
        @Body request: UpdateTruckOwnerStatusRequest,
    ): Response<TruckOwnerV2DetailResponse>

    @Multipart
    @POST("/api/v2/truck-owners")
    suspend fun createPersonalProfile(
        @Part fullName: MultipartBody.Part,
        @Part email: MultipartBody.Part?,
        @Part phone: MultipartBody.Part,
        @Part alternativePhones: MultipartBody.Part?,
        @Part panCard: MultipartBody.Part,
        @Part aadhaarCard: MultipartBody.Part,
        @Part aadhaarFile: MultipartBody.Part,
        @Part panFile: MultipartBody.Part,
    ): Response<TruckOwnerOnboardingV2Response>

    @Multipart
    @PUT("/api/v2/truck-owners/{userId}/personal")
    suspend fun updatePersonalProfile(
        @Path("userId") userId: String,
        @Part fullName: MultipartBody.Part,
        @Part email: MultipartBody.Part?,
        @Part phone: MultipartBody.Part,
        @Part alternativePhones: MultipartBody.Part?,
        @Part panCard: MultipartBody.Part,
        @Part aadhaarCard: MultipartBody.Part,
        @Part aadhaarFile: MultipartBody.Part?,
        @Part panFile: MultipartBody.Part?,
    ): Response<TruckOwnerOnboardingV2Response>

    @Multipart
    @PUT("/api/v2/truck-owners/{userId}/business")
    suspend fun updateBusinessProfile(
        @Path("userId") userId: String,
        @Part businessName: MultipartBody.Part?,
        @Part gstNumber: MultipartBody.Part?,
        @Part isTdsApplicable: MultipartBody.Part,
        @Part baseCityId: MultipartBody.Part?,
        @Part baseAreaId: MultipartBody.Part?,
        @Part preferredRoutes: MultipartBody.Part,
        @Part gstFile: MultipartBody.Part?,
        @Part companyFile: MultipartBody.Part?,
        @Part tdsFile: MultipartBody.Part?,
    ): Response<TruckOwnerOnboardingV2Response>

    @Multipart
    @PUT("/api/v2/truck-owners/{userId}/bank")
    suspend fun updateBankProfile(
        @Path("userId") userId: String,
        @Part accountHolderName: MultipartBody.Part,
        @Part accountNumber: MultipartBody.Part,
        @Part ifscCode: MultipartBody.Part,
        @Part upiId: MultipartBody.Part?,
        @Part bankName: MultipartBody.Part?,
        @Part branchName: MultipartBody.Part?,
        @Part chequeFile: MultipartBody.Part?,
    ): Response<TruckOwnerOnboardingV2Response>
}
