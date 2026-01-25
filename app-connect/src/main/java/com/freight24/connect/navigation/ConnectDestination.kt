package com.freight24.connect.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class ConnectDestination(val route: String) {

    open val arguments = emptyList<NamedNavArgument>()

    data object Login : ConnectDestination("login")
    data object Home : ConnectDestination("home")
    data object CreateIndent : ConnectDestination("createTrip")

    data object VerifyOtp : ConnectDestination("verifyOtp/{phone}") {
        const val PHONE_ARG = "phone"
        override val arguments = listOf(
            navArgument(PHONE_ARG) {
                type = NavType.StringType
            }
        )
        fun createRoute(phone: String): String {
            return "verifyOtp/$phone"
        }
    }
}

