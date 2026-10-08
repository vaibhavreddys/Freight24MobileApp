package com.freight24.connect.feature.trucks.enums

import com.freight24.connect.domain.model.TruckAssignmentOption

enum class TruckStatus(val displayText: String) {
    BLACKLISTED("Blacklisted"),
    OWNER_INACTIVE("Owner inactive"),
    IN_ACTIVE("Unavailable"),
    IN_TRANSIT("In Transit"),
    AVAILABLE("Available"),
    ;

    /** Only AVAILABLE trucks can be assigned to a trip. */
    val isAssignable: Boolean
        get() = this == AVAILABLE

    companion object {
        fun from(truck: TruckAssignmentOption): TruckStatus {
            return when {
                truck.isBlacklisted -> BLACKLISTED
                truck.ownerStatus?.equals("active", ignoreCase = true) == false -> OWNER_INACTIVE
                truck.status.lowercase() == "inactive" -> IN_ACTIVE
                truck.availability.equals("on_trip", ignoreCase = true) -> IN_TRANSIT
                else -> AVAILABLE
            }
        }
    }
}
