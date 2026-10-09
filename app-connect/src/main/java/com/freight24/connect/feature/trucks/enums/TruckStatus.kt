package com.freight24.connect.feature.trucks.enums

import com.freight24.connect.domain.model.TruckAssignmentOption

enum class TruckStatus(val displayText: String) {
    BLACKLISTED("Blacklisted"),
    OWNER_UNVERIFIED("Trucker unverified"),
    TRUCK_UNVERIFIED("Truck unverified"),
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
                truck.ownerStatus?.equals("active", ignoreCase = true) != true -> OWNER_UNVERIFIED
                !truck.isVerified -> TRUCK_UNVERIFIED
                truck.status.lowercase() == "inactive" -> IN_ACTIVE
                truck.availability.equals("on_trip", ignoreCase = true) -> IN_TRANSIT
                else -> AVAILABLE
            }
        }
    }
}
