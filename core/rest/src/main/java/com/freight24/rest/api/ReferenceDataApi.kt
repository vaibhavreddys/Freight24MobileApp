package com.freight24.rest.api

import com.freight24.rest.response.BranchTypeOptionDto
import com.freight24.rest.response.CityOptionDto
import com.freight24.rest.response.ContactRoleOptionDto
import com.freight24.rest.response.MasterDataListResponse
import com.freight24.rest.response.StateOptionDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/** Shared reference data is versioned independently from entity APIs. */
interface ReferenceDataApi {
    @GET("/api/v1/locations/states")
    suspend fun getStates(): Response<MasterDataListResponse<StateOptionDto>>

    @GET("/api/v1/locations/cities")
    suspend fun getCities(
        @Query("state_id") stateId: Int,
    ): Response<MasterDataListResponse<CityOptionDto>>

    @GET("/api/v1/masterData/branch-types")
    suspend fun getBranchTypes(
        @Query("is_active") isActive: Boolean = true,
    ): Response<MasterDataListResponse<BranchTypeOptionDto>>

    @GET("/api/v1/masterData/contact-roles")
    suspend fun getContactRoles(
        @Query("is_active") isActive: Boolean = true,
    ): Response<MasterDataListResponse<ContactRoleOptionDto>>
}
