package com.freight24.rest.api

import com.freight24.rest.response.ClientBranchesResponse
import com.freight24.rest.response.ClientContactsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

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
