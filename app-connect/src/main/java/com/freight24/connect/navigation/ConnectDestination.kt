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
        override val arguments =
            listOf(
                navArgument(PHONE_ARG) {
                    type = NavType.StringType
                },
            )

        fun createRoute(phone: String): String {
            return "verifyOtp/$phone"
        }
    }

    data object SearchSelection : ConnectDestination("search/{searchType}") {
        const val SEARCH_TYPE_ARG = "searchType"
        override val arguments =
            listOf(
                navArgument(SEARCH_TYPE_ARG) {
                    type = NavType.StringType
                },
            )

        fun createRoute(searchType: String): String {
            return "search/$searchType"
        }
    }

    data object ReviewIndent : ConnectDestination("reviewIndent")
}
