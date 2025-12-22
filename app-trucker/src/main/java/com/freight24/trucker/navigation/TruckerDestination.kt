package com.freight24.trucker.navigation

sealed class TruckerDestination(val route: String) {

    object Login : TruckerDestination("login")

    object VerifyOtp : TruckerDestination("verify_otp?phone={phone}") {
        fun createRoute(phone: String) =
            "verify_otp?phone=$phone"
    }

    object Dashboard : TruckerDestination("dashboard")

    object TripDetail : TruckerDestination("trip_detail/{tripId}") {
        fun createRoute(tripId: String) =
            "trip_detail/$tripId"
    }
}