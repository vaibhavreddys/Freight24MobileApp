package com.freight24.rest.api

import com.freight24.rest.response.ClientBranchesResponse
import com.freight24.rest.response.ClientContactsResponse
import com.freight24.rest.response.ClientContactCreateRequest
import com.freight24.rest.response.ClientContactCreateResponse
import com.freight24.rest.response.ClientOnboardingV2Response
import com.freight24.rest.response.ClientV2DetailResponse
import com.freight24.rest.response.ClientV2PageResponse
import com.freight24.rest.response.ClientV2ValidationResponse
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
 * V2 client operations. Paths are host-absolute because the Retrofit base URL
 * ends in `/api/v1/`.
 */
interface ClientV2Api {
    @GET("/api/v2/client")
    suspend fun getClients(
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 30,
    ): Response<ClientV2PageResponse>

    @GET("/api/v2/client/{clientId}")
    suspend fun getClientDetail(
        @Path("clientId") clientId: String,
    ): Response<ClientV2DetailResponse>

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

    @GET("/api/v2/client/validate")
    suspend fun validateClient(
        @Query("pan_card") panCard: String? = null,
        @Query("gst_number") gstNumber: String? = null,
    ): Response<ClientV2ValidationResponse>

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
