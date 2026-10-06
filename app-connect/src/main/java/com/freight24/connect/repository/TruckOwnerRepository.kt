package com.freight24.connect.repository

import com.freight24.rest.response.TruckOwnerOnboardingV2Response
import com.freight24.rest.response.TruckOwnerDto
import okhttp3.MultipartBody

interface TruckOwnerRepository {
    suspend fun getTruckOwners(): Result<List<TruckOwnerDto>>

    suspend fun searchTruckOwners(query: String): Result<List<TruckOwnerDto>>

    suspend fun createPersonalProfile(parts: List<MultipartBody.Part>): Result<TruckOwnerOnboardingV2Response>

    suspend fun updateBusinessProfile(
        ownerUserId: String,
        parts: List<MultipartBody.Part>,
    ): Result<TruckOwnerOnboardingV2Response>

    suspend fun updateBankProfile(
        ownerUserId: String,
        parts: List<MultipartBody.Part>,
    ): Result<TruckOwnerOnboardingV2Response>
}
