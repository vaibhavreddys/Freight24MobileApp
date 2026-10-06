package com.freight24.connect.repository

import com.freight24.connect.domain.model.EntityPage
import com.freight24.connect.domain.model.TruckOwnerCounts
import com.freight24.connect.domain.model.TruckOwnerDetails
import com.freight24.connect.domain.model.TruckOwnerManagementItem
import com.freight24.connect.domain.model.TruckOwnerPickerOption
import com.freight24.connect.domain.model.TruckOwnerValidation
import com.freight24.rest.response.TruckOwnerOnboardingV2Response
import okhttp3.MultipartBody

interface TruckOwnerRepository {
    suspend fun getManagementPage(
        query: String? = null,
        status: String? = null,
        documentStatus: String? = null,
        page: Int = 1,
        limit: Int = 30,
    ): Result<EntityPage<TruckOwnerManagementItem, TruckOwnerCounts>>

    suspend fun searchPickerOptions(
        query: String,
        page: Int = 1,
        limit: Int = 30,
    ): Result<EntityPage<TruckOwnerPickerOption, TruckOwnerCounts>>

    suspend fun getDetails(ownerUserId: String): Result<TruckOwnerDetails>

    suspend fun validate(
        phone: String? = null,
        pan: String? = null,
        aadhaar: String? = null,
        gst: String? = null,
        excludeOwnerUserId: String? = null,
    ): Result<TruckOwnerValidation>

    suspend fun updateStatus(ownerUserId: String, status: String): Result<TruckOwnerDetails>

    suspend fun createPersonalProfile(parts: List<MultipartBody.Part>): Result<TruckOwnerOnboardingV2Response>

    suspend fun updatePersonalProfile(
        ownerUserId: String,
        parts: List<MultipartBody.Part>,
    ): Result<TruckOwnerOnboardingV2Response>

    suspend fun updateBusinessProfile(
        ownerUserId: String,
        parts: List<MultipartBody.Part>,
    ): Result<TruckOwnerOnboardingV2Response>

    suspend fun updateBankProfile(
        ownerUserId: String,
        parts: List<MultipartBody.Part>,
    ): Result<TruckOwnerOnboardingV2Response>
}
