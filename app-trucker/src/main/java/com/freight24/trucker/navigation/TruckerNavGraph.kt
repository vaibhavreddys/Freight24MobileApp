package com.freight24.trucker.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.freight24.trucker.feature.login.LoginScreen
import com.freight24.trucker.feature.login.VerifyOtpScreen
import com.freight24.trucker.feature.main.TruckerMainScreen
import com.freight24.trucker.feature.trips.details.TripDetailsScreen

@Composable
fun TruckerNavGraph(
    navController: NavHostController,
    startDestination: String,
    snackbarHostState: androidx.compose.material3.SnackbarHostState
) {

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 1. Login Screen
        composable(TruckerDestination.Login.route) {
            LoginScreen(
                navController = navController,
                viewModel = hiltViewModel(),
                snackbarHostState = snackbarHostState
            )
        }

        // 2. Verify OTP
        composable(
            route = TruckerDestination.VerifyOtp.route,
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""

            VerifyOtpScreen(
                navController = navController,
                phoneNumber = phone,
                snackbarHostState = snackbarHostState,
                onLoginSuccess = {
                    navController.navigate(TruckerDestination.Main.route) {
                        popUpTo(TruckerDestination.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // 3. LoadBoard (Main Container)
        composable(TruckerDestination.Main.route) {
            TruckerMainScreen(
                navController = navController,
                snackbarHostState = snackbarHostState
            )
        }

        // 4. Trip detail
        composable(
            route = TruckerDestination.TripDetail.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType }
            )
        ) {
            TripDetailsScreen(
                onBackClick = { navController.popBackStack() },
                snackbarHostState = snackbarHostState,
                onNavigateToPayments = { tripId ->
                    // Placeholder for now
                }
            )
        }
    }
}