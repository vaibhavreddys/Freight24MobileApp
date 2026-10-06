package com.freight24.connect.repository

import com.freight24.connect.domain.model.EntityPage
import com.freight24.connect.domain.model.TruckAssignmentOption
import com.freight24.connect.domain.model.TruckCounts
import com.freight24.connect.domain.model.TruckDetails
import com.freight24.connect.domain.model.TruckManagementItem
import com.freight24.connect.domain.model.TruckRegistrationValidation
import com.freight24.rest.response.CreateTruckV2Response
import okhttp3.MultipartBody

interface TruckRepository {
    suspend fun getTruckManagementPage(
        query: String? = null,
        status: String? = null,
        documentStatus: String? = null,
        page: Int = 1,
        limit: Int = 30,
    ): Result<EntityPage<TruckManagementItem, TruckCounts>>

    suspend fun searchAssignmentTrucks(
        query: String,
        page: Int = 1,
        limit: Int = 30,
    ): Result<EntityPage<TruckAssignmentOption, TruckCounts>>

    suspend fun getTruckDetails(truckId: String): Result<TruckDetails>

    suspend fun validateRegistration(
        registrationNumber: String,
        excludeTruckId: String? = null,
    ): Result<TruckRegistrationValidation>

    suspend fun getTruckDocumentSignedUrl(
        truckId: String,
        documentType: String,
    ): Result<String>

    suspend fun addTruck(
        registrationNumber: MultipartBody.Part,
        truckTypeId: MultipartBody.Part,
        ownerProfileId: MultipartBody.Part,
        make: MultipartBody.Part?,
        model: MultipartBody.Part?,
        year: MultipartBody.Part?,
        capacityMt: MultipartBody.Part?,
        rcCard: MultipartBody.Part?,
        fcCertificate: MultipartBody.Part?,
        insurance: MultipartBody.Part?,
    ): Result<CreateTruckV2Response>

    suspend fun updateTruck(
        truckId: String,
        registrationNumber: MultipartBody.Part,
        truckTypeId: MultipartBody.Part,
        make: MultipartBody.Part?,
        model: MultipartBody.Part?,
        year: MultipartBody.Part?,
        capacityMt: MultipartBody.Part?,
        rcCard: MultipartBody.Part? = null,
        fcCertificate: MultipartBody.Part? = null,
        insurance: MultipartBody.Part? = null,
    ): Result<TruckDetails>

}
