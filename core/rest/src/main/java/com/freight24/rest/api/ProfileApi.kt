package com.freight24.rest.api

import com.freight24.rest.request.FinalSaveRequest
import com.freight24.rest.request.DiscardTemporaryDocumentsRequest
import com.freight24.rest.response.BaseResponse
import com.freight24.rest.response.IdentityDocumentUploadResponse
import com.freight24.rest.response.ProfileResponse
import com.freight24.rest.response.IfscValidationResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfileApi {
    @GET("bank/ifsc/validate")
    suspend fun validateIfsc(
        @Query("ifsc") ifsc: String,
    ): Response<IfscValidationResponse>

    @GET("truck-owners/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @PUT("truck-owners/{id}")
    suspend fun updateProfile(
        @Path("id") id: String,
        @Body request: com.freight24.rest.request.UpdateProfileRequest,
    ): Response<BaseResponse>

    @Multipart
    @POST("documents/identity/add")
    suspend fun uploadIdentityDocument(
        @Part file: MultipartBody.Part,
        @Part("document_type_id") documentTypeId: Int,
        @Part("user_id") userId: String,
        @Part("document_number") documentNumber: okhttp3.RequestBody,
    ): Response<BaseResponse>

    @Multipart
    @PUT("documents/identity/update/{docId}/{userId}")
    suspend fun updateIdentityDocument(
        @Path("docId") docId: String,
        @Path("userId") userId: String,
        @Part file: MultipartBody.Part,
        @Part("document_number") documentNumber: okhttp3.RequestBody,
    ): Response<BaseResponse>

    @Multipart
    @POST("documents/identity/add")
    suspend fun uploadIdentityDocumentWithTemporaryKey(
        @Part file: MultipartBody.Part,
        @Part("document_type_id") documentTypeId: Int,
        @Part("user_id") userId: String,
        @Part("document_number") documentNumber: okhttp3.RequestBody,
    ): Response<IdentityDocumentUploadResponse>

    @Multipart
    @PUT("documents/identity/update/{docId}/{userId}")
    suspend fun updateIdentityDocumentWithTemporaryKey(
        @Path("docId") docId: String,
        @Path("userId") userId: String,
        @Part file: MultipartBody.Part,
        @Part("document_number") documentNumber: okhttp3.RequestBody,
    ): Response<IdentityDocumentUploadResponse>

    @POST("documents/identity/final-save")
    suspend fun finalizeIdentityDocuments(
        @Body request: FinalSaveRequest,
    ): Response<BaseResponse>

    @POST("documents/identity/discard-temporary")
    suspend fun discardTemporaryDocuments(
        @Body request: DiscardTemporaryDocumentsRequest,
    ): Response<BaseResponse>

    @Multipart
    @POST("documents/bank/add")
    suspend fun addBankAccount(
        @Part parts: List<MultipartBody.Part>,
    ): Response<BaseResponse>

    @Multipart
    @PUT("documents/bank/update/{accountId}/{userId}")
    suspend fun updateBankAccount(
        @Path("accountId") accountId: String,
        @Path("userId") userId: String,
        @Part parts: List<MultipartBody.Part>,
    ): Response<BaseResponse>
}
