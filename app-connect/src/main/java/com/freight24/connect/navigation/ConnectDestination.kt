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

    data object IndentSuccess : ConnectDestination("indentSuccess/{indentId}") {
        const val INDENT_ID_ARG = "indentId"
        override val arguments =
            listOf(
                navArgument(INDENT_ID_ARG) {
                    type = NavType.StringType
                },
            )

        fun createRoute(indentId: String): String {
            return "indentSuccess/$indentId"
        }
    }

    data object Indents : ConnectDestination("indents")
    data object IndentDetails : ConnectDestination("indent_details/{indentId}") {
        const val INDENT_ID_ARG = "indentId"
        override val arguments = listOf(
            navArgument(INDENT_ID_ARG) { type = NavType.StringType }
        )
        fun createRoute(indentId: String) = "indent_details/$indentId"
    }

    data object EditIndent : ConnectDestination("edit_indent/{indentId}") {
        const val INDENT_ID_ARG = "indentId"
        override val arguments = listOf(
            navArgument(INDENT_ID_ARG) { type = NavType.StringType }
        )
        fun createRoute(indentId: String) = "edit_indent/$indentId"
    }

    data object AssignTruck : ConnectDestination("assign_truck/{indentId}") {
        const val INDENT_ID_ARG = "indentId"
        override val arguments = listOf(
            navArgument(INDENT_ID_ARG) { type = NavType.StringType }
        )
        fun createRoute(indentId: String) = "assign_truck/$indentId"
    }

    data object CreateTrip : ConnectDestination("create_trip/{indentId}/{truckId}") {
        const val INDENT_ID_ARG = "indentId"
        const val TRUCK_ID_ARG = "truckId"
        override val arguments = listOf(
            navArgument(INDENT_ID_ARG) { type = NavType.StringType },
            navArgument(TRUCK_ID_ARG) { type = NavType.StringType }
        )
        fun createRoute(indentId: String, truckId: String) = "create_trip/$indentId/$truckId"
    }

    data object TripSuccess : ConnectDestination(
        "trip_success/{tripId}?truckNumber={truckNumber}&origin={origin}&destination={destination}&driverName={driverName}"
    ) {
        const val TRIP_ID_ARG = "tripId"
        const val TRUCK_NUMBER_ARG = "truckNumber"
        const val ORIGIN_ARG = "origin"
        const val DESTINATION_ARG = "destination"
        const val DRIVER_NAME_ARG = "driverName"

        override val arguments = listOf(
            navArgument(TRIP_ID_ARG) { type = NavType.StringType },
            navArgument(TRUCK_NUMBER_ARG) { type = NavType.StringType },
            navArgument(ORIGIN_ARG) { type = NavType.StringType },
            navArgument(DESTINATION_ARG) { type = NavType.StringType },
            navArgument(DRIVER_NAME_ARG) { type = NavType.StringType }
        )

        fun createRoute(
            tripId: String,
            truckNumber: String,
            origin: String,
            destination: String,
            driverName: String
        ): String {
            return "trip_success/$tripId?truckNumber=$truckNumber&origin=$origin&destination=$destination&driverName=$driverName"
        }
    }
}
