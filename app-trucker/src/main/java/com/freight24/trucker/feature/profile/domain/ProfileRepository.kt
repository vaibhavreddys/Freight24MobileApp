package com.freight24.trucker.feature.profile.domain

interface ProfileRepository {
    suspend fun getProfile(): Result<Profile>
    suspend fun updateProfile(id: String, request: com.freight24.rest.request.UpdateProfileRequest): Result<Boolean>
    suspend fun uploadIdentityDocument(userId: String, documentTypeId: Int, documentNumber: String, file: okhttp3.MultipartBody.Part): Result<Boolean>
    suspend fun updateIdentityDocument(docId: String, userId: String, documentNumber: String, file: okhttp3.MultipartBody.Part): Result<Boolean>
    suspend fun getGenericSignedUrl(s3Url: String): Result<String>
}
