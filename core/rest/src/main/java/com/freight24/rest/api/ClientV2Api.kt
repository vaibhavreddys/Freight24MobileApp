package com.freight24.rest.api

import com.freight24.rest.response.ClientBranchesResponse
import com.freight24.rest.response.ClientContactsResponse
import com.freight24.rest.response.BranchTypeOptionDto
import com.freight24.rest.response.CityOptionDto
import com.freight24.rest.response.ClientContactCreateRequest
import com.freight24.rest.response.ClientContactCreateResponse
import com.freight24.rest.response.ClientOnboardingV2Response
import com.freight24.rest.response.ContactRoleOptionDto
import com.freight24.rest.response.MasterDataListResponse
import com.freight24.rest.response.StateOptionDto
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

/**
 * Read-only slice of the v2 client API — just what the indent flow needs to
 * populate its client branch and contact pickers.
 *
 * The rest of the v2 surface (client create/update, onboarding, documents,
 * verification, branch write ops) is deliberately **not** modelled here. The
 * existing [ClientApi] still serves the legacy `/api/v1/client` screens; moving
 * those across is a separate migration.
 *
 * Paths are host-absolute (leading `/`) because the Retrofit base URL ends in
 * `/api/v1/` while these routes are mounted at `/api/v2/client`.
 */
interface ClientV2Api {
    @Multipart
    @POST("/api/v2/client")
    suspend fun createClient(
        @Part parts: List<MultipartBody.Part>,
    ): Response<ClientOnboardingV2Response>

    @Multipart
    @PUT("/api/v2/client/{clientId}/onboarding/locations")
    suspend fun saveLocations(
        @Path("clientId") clientId: String,
        @Part parts: List<MultipartBody.Part>,
    ): Response<ClientOnboardingV2Response>

    @POST("/api/v2/client/{clientId}/contacts")
    suspend fun createContact(
        @Path("clientId") clientId: String,
        @Body request: ClientContactCreateRequest,
    ): Response<ClientContactCreateResponse>

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

    /** Active and inactive branches alike — the caller filters on [status]. */
    @GET("/api/v2/client/{clientId}/branches")
    suspend fun getClientBranches(
        @Path("clientId") clientId: String,
    ): Response<ClientBranchesResponse>

    /**
     * Active contacts by default. The backend reads `is_active` as the string
     * `"true"`/`"false"` and defaults to active when the query param is absent.
     */
    @GET("/api/v2/client/{clientId}/contacts")
    suspend fun getClientContacts(
        @Path("clientId") clientId: String,
    ): Response<ClientContactsResponse>
}
