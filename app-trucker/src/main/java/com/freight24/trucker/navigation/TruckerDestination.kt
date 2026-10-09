package com.freight24.trucker.navigation

sealed class TruckerDestination(val route: String) {
    object Login : TruckerDestination("login")

    object VerifyOtp : TruckerDestination("verifyOtp/{phone}") {
        fun createRoute(phone: String) = "verifyOtp/$phone"
    }

    // NEW authenticated root
    object Main : TruckerDestination("main")

    // (Optional – keep for later deep links)
    object TripDetail : TruckerDestination("tripDetail/{tripId}") {
        fun createRoute(tripId: String) = "tripDetail/$tripId"
    }

    object TruckDetail : TruckerDestination("truckDetail/{truckId}") {
        fun createRoute(truckId: String) = "truckDetail/$truckId"
    }

    object OnboardTruck : TruckerDestination("onboardTruck")

    object EditTruck : TruckerDestination("editTruck/{truckId}") {
        fun createRoute(truckId: String) = "editTruck/$truckId"
    }

    object Settings : TruckerDestination("settings")

    object BaseCitySearch : TruckerDestination("profile/base-city")
}
