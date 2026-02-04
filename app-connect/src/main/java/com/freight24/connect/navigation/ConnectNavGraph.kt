package com.freight24.connect.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.freight24.connect.feature.home.HomeScreen
import com.freight24.connect.feature.indents.views.CreateIndentScreen
import com.freight24.connect.feature.indents.views.EditIndentScreen
import com.freight24.connect.feature.indents.views.IndentDetailsScreen
import com.freight24.connect.feature.indents.views.IndentSuccessScreen
import com.freight24.connect.feature.indents.views.ReviewIndentScreen
import com.freight24.connect.feature.trucks.views.AssignTruckScreen
import com.freight24.connect.feature.trips.views.CreateTripScreen
import com.freight24.connect.feature.trips.views.TripSuccessScreen
import com.freight24.connect.feature.indents.views.SearchSelectionScreen
import com.freight24.connect.feature.indents.views.SearchType
import com.freight24.connect.feature.login.view.signin.LoginScreen
import com.freight24.connect.feature.login.view.verify.VerifyOtpScreen

@Composable
fun ConnectNavGraph(
    navHostController: NavHostController,
    startDestination: String = ConnectDestination.Login.route,
) {
    NavHost(
        navController = navHostController,
        startDestination = startDestination,
    ) {
        composable(ConnectDestination.Login.route) {
            LoginScreen(onVerify = { phoneNumber ->
                navHostController.navigate(
                    ConnectDestination.VerifyOtp.createRoute(phoneNumber),
                )
            })
        }

        composable(
            route = ConnectDestination.VerifyOtp.route,
            arguments = ConnectDestination.VerifyOtp.arguments,
        ) { backStackEntry ->

            val phoneNumber =
                backStackEntry
                    .arguments
                    ?.getString(ConnectDestination.VerifyOtp.PHONE_ARG)

            VerifyOtpScreen(
                navController = navHostController,
                phoneNumber = phoneNumber ?: "",
                onLoginSuccess = {
                    navHostController.navigate(ConnectDestination.Home.route) {
                        // Clear the back stack to prevent going back to the auth flow
                        popUpTo(ConnectDestination.Login.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(ConnectDestination.Home.route) {
            HomeScreen(
                onCreateIndentClick = {
                    navHostController.navigate(ConnectDestination.CreateIndent.route)
                },
                onIndentClick = { indentId ->
                    navHostController.navigate(ConnectDestination.IndentDetails.createRoute(indentId))
                }
            )
        }

        composable(ConnectDestination.CreateIndent.route) { backStackEntry ->
            CreateIndentScreen(
                onBackClick = {
                    navHostController.popBackStack()
                },
                onNavigateToSearch = { searchType ->
                    navHostController.navigate(ConnectDestination.SearchSelection.createRoute(searchType))
                },
                onNavigateToReview = {
                    navHostController.navigate(ConnectDestination.ReviewIndent.route)
                },
                navController = navHostController,
            )
        }

        composable(
            route = ConnectDestination.SearchSelection.route,
            arguments = ConnectDestination.SearchSelection.arguments,
        ) { backStackEntry ->
            val searchTypeString =
                backStackEntry
                    .arguments
                    ?.getString(ConnectDestination.SearchSelection.SEARCH_TYPE_ARG) ?: "CLIENT"

            val searchType = SearchType.valueOf(searchTypeString)

            SearchSelectionScreen(
                searchType = searchType,
                onBackClick = {
                    navHostController.popBackStack()
                },
                navController = navHostController,
            )
        }

        composable(ConnectDestination.ReviewIndent.route) {
            ReviewIndentScreen(
                onBackClick = {
                    navHostController.popBackStack()
                },
                onEditClick = {
                    navHostController.popBackStack()
                },
                navController = navHostController,
            )
        }

        composable(
            route = ConnectDestination.IndentSuccess.route,
            arguments = ConnectDestination.IndentSuccess.arguments,
        ) { backStackEntry ->
            val indentId =
                backStackEntry
                    .arguments
                    ?.getString(ConnectDestination.IndentSuccess.INDENT_ID_ARG) ?: ""

            IndentSuccessScreen(
                navController = navHostController,
                indentId = indentId,
                onViewIndentClick = {
                    navHostController.navigate(ConnectDestination.Home.route) {
                        popUpTo(ConnectDestination.CreateIndent.route) { inclusive = true }
                    }
                },
                onBackToHomeClick = {
                    navHostController.navigate(ConnectDestination.Home.route) {
                        popUpTo(ConnectDestination.CreateIndent.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = ConnectDestination.IndentDetails.route,
            arguments = ConnectDestination.IndentDetails.arguments,
        ) { backStackEntry ->
            val indentId = backStackEntry.arguments?.getString(ConnectDestination.IndentDetails.INDENT_ID_ARG) ?: ""
            IndentDetailsScreen(
                navController = navHostController,
                indentId = indentId,
                onBackClick = {
                    navHostController.popBackStack()
                },
                onEditClick = { id ->
                    navHostController.navigate(ConnectDestination.EditIndent.createRoute(id))
                },
                onCreateTripClick = { id ->
                    navHostController.navigate(ConnectDestination.AssignTruck.createRoute(id))
                }
            )
        }

        composable(
            route = ConnectDestination.EditIndent.route,
            arguments = ConnectDestination.EditIndent.arguments,
        ) { backStackEntry ->
            val indentId = backStackEntry.arguments?.getString(ConnectDestination.EditIndent.INDENT_ID_ARG) ?: ""
            EditIndentScreen(
                indentId = indentId,
                onBackClick = { navHostController.popBackStack() },
                onSuccess = {
                    navHostController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navHostController.popBackStack()
                }
            )
        }

        composable(
            route = ConnectDestination.AssignTruck.route,
            arguments = ConnectDestination.AssignTruck.arguments,
        ) { backStackEntry ->
            val indentId = backStackEntry.arguments?.getString(ConnectDestination.AssignTruck.INDENT_ID_ARG) ?: ""
            AssignTruckScreen(
                onBackClick = { navHostController.popBackStack() },
                onTruckSelected = { truckId ->
                   navHostController.navigate(ConnectDestination.CreateTrip.createRoute(indentId, truckId))
                }
            )
        }

        composable(
            route = ConnectDestination.CreateTrip.route,
            arguments = ConnectDestination.CreateTrip.arguments,
        ) { backStackEntry ->
            val indentId = backStackEntry.arguments?.getString(ConnectDestination.CreateTrip.INDENT_ID_ARG) ?: ""
            val truckId = backStackEntry.arguments?.getString(ConnectDestination.CreateTrip.TRUCK_ID_ARG) ?: ""
            CreateTripScreen(
                indentId = indentId,
                truckId = truckId,
                onBackClick = { navHostController.popBackStack() },
                onTripCreated = { tripId, truckNumber, origin, destination, driverName ->
                    val route = ConnectDestination.TripSuccess.createRoute(tripId, truckNumber, origin, destination, driverName)
                    navHostController.navigate(route) {
                        popUpTo(ConnectDestination.CreateTrip.route) { inclusive = true } // Remove create trip from stack
                    }
                }
            )
        }

        composable(
            route = ConnectDestination.TripSuccess.route,
            arguments = ConnectDestination.TripSuccess.arguments,
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString(ConnectDestination.TripSuccess.TRIP_ID_ARG) ?: ""
            val truckNumber = backStackEntry.arguments?.getString(ConnectDestination.TripSuccess.TRUCK_NUMBER_ARG) ?: ""
            val origin = backStackEntry.arguments?.getString(ConnectDestination.TripSuccess.ORIGIN_ARG) ?: ""
            val destination = backStackEntry.arguments?.getString(ConnectDestination.TripSuccess.DESTINATION_ARG) ?: ""
            val driverName = backStackEntry.arguments?.getString(ConnectDestination.TripSuccess.DRIVER_NAME_ARG) ?: ""

            TripSuccessScreen(
                tripId = tripId,
                truckNumber = truckNumber,
                origin = origin,
                destination = destination,
                driverName = driverName,
                onGoToTripsClick = {
                    navHostController.navigate(ConnectDestination.Indents.route) {
                        popUpTo(ConnectDestination.Indents.route) { inclusive = true }
                    }
                },
                onBackToDashboardClick = {
                     navHostController.navigate(ConnectDestination.Indents.route) {
                        popUpTo(ConnectDestination.Indents.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
