package com.freight24.rest.api

import com.freight24.rest.response.TruckOwnerOnboardingV2Response
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface TruckOwnerV2Api {
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
    @PUT("/api/v2/truck-owners/{userId}/business")
    suspend fun updateBusinessProfile(
        @Path("userId") userId: String,
        @Part businessName: MultipartBody.Part?,
        @Part gstNumber: MultipartBody.Part?,
        @Part isTdsApplicable: MultipartBody.Part,
        @Part baseCityId: MultipartBody.Part?,
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
        @Part chequeFile: MultipartBody.Part,
    ): Response<TruckOwnerOnboardingV2Response>
}
