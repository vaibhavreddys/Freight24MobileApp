package com.freight24.connect.feature.trucks.enums

import androidx.compose.ui.graphics.Color
import com.freight24.rest.response.SearchTruckResponse
import com.freight24.rest.utils.isInActive
import com.freight24.rest.utils.isOnTrip

enum class TruckStatus(val displayText: String, val color: Color) {
    BLACKLISTED("Blacklisted", Color(0xFFFF0000)),
    IN_TRANSIT("In Transit", Color(0xFFFFAB00)),
    AVAILABLE("Available", Color(0xFF00C853));

    companion object {
        fun from(truck: SearchTruckResponse.Truck): TruckStatus {
            return when {
                truck.isBlacklisted || truck.isInActive() -> BLACKLISTED
                truck.isOnTrip() -> IN_TRANSIT
                else -> AVAILABLE
            }
        }
    }
}