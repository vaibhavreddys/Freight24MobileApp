package com.freight24.connect.feature.truckowners.profile.domain


interface ProfileRepository {
    suspend fun getProfile(userId: String): Result<Profile>

    suspend fun updateTruckOwnerStatus(
        userId: String,
        status: String,
    ): Result<Boolean>

    suspend fun getIdentityDocumentSignedUrl(documentId: String): Result<String>
}
