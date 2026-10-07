package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Client branch and contact reads from the v2 client API (backend commits
 * `aeee64a` + `60b47bc`).
 *
 * **Naming:** every type here carries a `V2` suffix so branch/contact endpoint
 * payloads remain distinct from the nested client-detail response types.
 *
 * **Path note:** these live under `/api/v2/client`, while the app's Retrofit
 * base URL ends in `/api/v1/`. The endpoints therefore declare host-absolute
 * paths (leading `/`), which Retrofit resolves against the host root rather
 * than the base path.
 *
 * **Key-style trap:** branches come back camelCase, but contacts come back
 * **snake_case** — the same entity is serialised two different ways depending
 * on the endpoint. `GET /api/v2/client/:id` embeds contacts as `fullName` /
 * `alternatePhone` / `isPrimary`; `GET /api/v2/client/:id/contacts` returns
 * `full_name` / `alternate_phone` / `is_primary`. The `@SerialName`
 * annotations below follow the *contacts endpoint* shape, which is what the
 * pickers read.
 */

// ---------------------------------------------------------------------------
// Branches — GET /api/v2/client/:clientId/branches
// ---------------------------------------------------------------------------

@Serializable
data class ClientBranchesResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: ClientBranchesData? = null,
)

@Serializable
data class ClientBranchesData(
    val clientId: String? = null,
    val branches: List<ClientBranchV2Dto> = emptyList(),
    val branchCount: Int? = null,
)

@Serializable
data class ClientBranchV2Dto(
    val id: String,
    val branchName: String? = null,
    val branchCode: String? = null,
    val branchType: ClientBranchTypeV2Dto? = null,
    /** `active` | `inactive`. Only active branches are valid on an indent. */
    val status: String? = null,
    val address: ClientBranchAddressV2Dto? = null,
)

@Serializable
data class ClientBranchTypeV2Dto(
    val id: String? = null,
    val code: String? = null,
    val name: String? = null,
    val description: String? = null,
)

@Serializable
data class ClientBranchAddressV2Dto(
    val line1: String? = null,
    val line2: String? = null,
    val area: ClientAddressNodeV2Dto? = null,
    val city: ClientAddressNodeV2Dto? = null,
    val state: ClientAddressNodeV2Dto? = null,
    val pinCode: String? = null,
)

@Serializable
data class ClientAddressNodeV2Dto(
    val id: String? = null,
    val name: String? = null,
)

// ---------------------------------------------------------------------------
// Contacts — GET /api/v2/client/:clientId/contacts
// ---------------------------------------------------------------------------

@Serializable
data class ClientContactsResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: ClientContactsData? = null,
)

@Serializable
data class ClientContactsData(
    val total: Int? = null,
    val data: List<ClientContactV2Dto> = emptyList(),
)

@Serializable
data class ClientContactV2Dto(
    val id: String,
    @SerialName("full_name") val fullName: String? = null,
    val designation: String? = null,
    val phone: String? = null,
    @SerialName("alternate_phone") val alternatePhone: String? = null,
    val email: String? = null,
    @SerialName("is_primary") val isPrimary: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("client_id") val clientId: String? = null,
)
