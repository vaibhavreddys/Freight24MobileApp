package com.freight24.connect.feature.trucks.enums

import com.freight24.rest.response.SearchTruckResponse

enum class TruckStatus(val displayText: String) {
    BLACKLISTED("Blacklisted"),
    IN_ACTIVE("In Active"),
    IN_TRANSIT("In Transit"),
    AVAILABLE("Available"),
    ;

    companion object {
        fun from(truck: SearchTruckResponse.Truck): TruckStatus {
            return when {
                truck.isBlacklisted -> BLACKLISTED
                truck.status.lowercase() == "inactive" -> IN_ACTIVE
                truck.availability.lowercase() == "on_trip" -> IN_TRANSIT
                else -> AVAILABLE
            }
        }
    }
}
