package com.freight24.rest.api

import com.freight24.rest.response.SignedDocument
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DocumentApi {
    @GET("documents/identity-documents/{documentId}/signed-url")
    suspend fun getIdentityDocumentSignedUrl(
        @Path("documentId") documentId: String,
    ): Response<SignedDocument>

    @GET("documents/trucks/{truckId}/documents/{documentType}/signed-url")
    suspend fun getTruckDocumentSignedUrl(
        @Path("truckId") truckId: String,
        @Path("documentType") documentType: String,
    ): Response<SignedDocument>

    @GET("documents/clients/{clientId}/documents/{documentId}/signed-url")
    suspend fun getClientDocumentSignedUrl(
        @Path("clientId") clientId: String,
        @Path("documentId") documentId: String,
    ): Response<SignedDocument>

    @GET("documents/clients/{clientId}/branches/{branchId}/documents/{documentId}/signed-url")
    suspend fun getClientBranchDocumentSignedUrl(
        @Path("clientId") clientId: String,
        @Path("branchId") branchId: String,
        @Path("documentId") documentId: String,
    ): Response<SignedDocument>

    @GET("documents/verification-requests/{entity}/{requestId}/documents/{documentKey}/signed-url")
    suspend fun getVerificationRequestDocumentSignedUrl(
        @Path("entity") entity: String,
        @Path("requestId") requestId: String,
        @Path("documentKey") documentKey: String,
    ): Response<SignedDocument>
}
