package com.freight24.connect.feature.truckowners.profile.domain

import com.freight24.rest.request.FinalSaveRequest

interface ProfileRepository {
    suspend fun getProfile(userId: String): Result<Profile>

    suspend fun updateTruckOwnerStatus(
        userId: String,
        status: String,
    ): Result<Boolean>

    suspend fun uploadIdentityDocument(
        userId: String,
        documentTypeId: Int,
        documentNumber: String,
        file: okhttp3.MultipartBody.Part,
    ): Result<String>

    suspend fun updateIdentityDocument(
        docId: String,
        userId: String,
        documentNumber: String,
        file: okhttp3.MultipartBody.Part,
    ): Result<String>

    suspend fun finalizeIdentityDocuments(request: FinalSaveRequest): Result<Boolean>

    suspend fun getIdentityDocumentSignedUrl(documentId: String): Result<String>
}
