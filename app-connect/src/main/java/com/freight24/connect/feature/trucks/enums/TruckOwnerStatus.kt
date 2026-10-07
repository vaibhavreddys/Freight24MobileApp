package com.freight24.connect.feature.trucks.enums

/**
 * Activation state of a truck owner as reported by `truck_owner_profiles.status`
 * on `GET /api/v2/truck-owners/basic/search`.
 *
 * The column is a text CHECK constraint limited to `active` / `inactive` and
 * defaults to `inactive`, so anything that is not explicitly `active` is treated
 * as inactive (fail-closed) — a truck may only be added under an active owner.
 */
enum class TruckOwnerStatus(val displayText: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    ;

    /** Only ACTIVE truck owners can own a newly added truck. */
    val isSelectable: Boolean
        get() = this == ACTIVE

    companion object {
        fun from(status: String?): TruckOwnerStatus =
            if (status?.trim()?.lowercase() == "active") ACTIVE else INACTIVE
    }
}