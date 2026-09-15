package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The four contact roles a client puts on an indent — who the driver calls for
 * placement, payment, operations and POD.
 *
 * Backend: `POST /api/v1/indents/groups` → `contacts`, persisted to
 * `indent_contacts` under a `UNIQUE(indent_group_id, contact_type)` constraint.
 * Each value is a `client_contacts.id` that must belong to the indent's client
 * and be active.
 *
 * `placement` is **required** by the backend; the other three are optional.
 * All four are nullable because the same shape is reused by `PUT
 * /api/v1/indents/groups/:id`, where a partial update only sends the roles the
 * operator actually changed.
 */
@Serializable
data class IndentContactsRequest(
    @SerialName("placement") val placement: String? = null,
    @SerialName("payment") val payment: String? = null,
    @SerialName("operations") val operations: String? = null,
    @SerialName("pod") val pod: String? = null,
) {
    /** True when no role is set — omit `contacts` entirely rather than send `{}`. */
    val isEmpty: Boolean
        get() = placement == null && payment == null && operations == null && pod == null
}
