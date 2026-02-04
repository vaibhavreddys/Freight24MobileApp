package com.freight24.rest.utils

import com.freight24.rest.response.SearchTruckResponse

fun SearchTruckResponse.Truck.isEnabled() : Boolean {
    return !this.isBlacklisted
            && this.availability.lowercase() != "on_trip"
            && this.status.lowercase() != "inactive"
}

fun SearchTruckResponse.Truck.isInActive() : Boolean {
    return this.status.lowercase() == "inactive"
}

fun SearchTruckResponse.Truck.isOnTrip() : Boolean {
    return this.availability.lowercase() == "on_trip"
}