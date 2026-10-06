package com.freight24.connect.feature.trucks.enums

import com.freight24.connect.domain.model.TruckAssignmentOption

enum class TruckStatus(val displayText: String) {
    BLACKLISTED("Blacklisted"),
    IN_ACTIVE("In Active"),
    IN_TRANSIT("In Transit"),
    AVAILABLE("Available"),
    ;

    companion object {
        fun from(truck: TruckAssignmentOption): TruckStatus {
            return when {
                truck.isBlacklisted -> BLACKLISTED
                truck.status.lowercase() == "inactive" -> IN_ACTIVE
                truck.availability.equals("on_trip", ignoreCase = true) -> IN_TRANSIT
                else -> AVAILABLE
            }
        }
    }
}
