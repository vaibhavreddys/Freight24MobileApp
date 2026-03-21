package com.freight24.connect.feature.trucks.enums

import androidx.compose.ui.graphics.Color
import com.freight24.rest.response.SearchTruckResponse

enum class TruckStatus(val displayText: String, val color: Color) {
    BLACKLISTED("Blacklisted", Color(0xFFFF0000)),
    IN_ACTIVE("In Active", Color(0xFFFF0000)),
    IN_TRANSIT("In Transit", Color(0xFFFFAB00)),
    AVAILABLE("Available", Color(0xFF00C853));

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
