package com.freight24.trucker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.freight24.trucker.feature.home.TruckerHomeScreen
import com.freight24.trucker.feature.login.view.signin.LoginScreen
import com.freight24.trucker.feature.login.view.verify.VerifyOtpScreen
import com.freight24.trucker.feature.trips.view.details.TripDetailsScreen
import com.freight24.trucker.feature.trucks.view.details.TruckDetailsScreen
import com.freight24.trucker.feature.trucks.view.onboard.OnboardTruckScreen

@Composable
fun TruckerNavGraph(
    navController: NavHostController,
    startDestination: String,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // 1. Login Screen
        composable(TruckerDestination.Login.route) {
            LoginScreen(
                navController = navController,
            )
            // Replace with your actual LoginScreen composable
//            LoginScreen()
//             LoginScreen(
//                 onNavigateToVerify = { phone ->
//                     navController.navigate(TruckerDestination.VerifyOtp.createRoute(phone))
//                 }
//             )
        }

        // 2️⃣ Verify OTP
        composable(
            route = TruckerDestination.VerifyOtp.route,
            arguments =
                listOf(
                    navArgument("phone") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""

            VerifyOtpScreen(
                navController = navController,
                phoneNumber = phone,
                onLoginSuccess = {
                    navController.navigate(TruckerDestination.Main.route) {
                        popUpTo(TruckerDestination.Login.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }
//        // 2. Verify OTP Screen
//        composable(
//            route = TruckerDestination.VerifyOtp.route,
//            arguments = listOf(
//                navArgument("phone") { type = NavType.StringType }
//            )
//        ) { backStackEntry ->
//            val phone = backStackEntry.arguments?.getString("phone") ?: ""
//            VerifyOtpScreen(
//                navController = navController,
//                phoneNumber = phone
//                // onLoginSuccess = { ... navigate to dashboard }
//            )
//            // Replace with your actual VerifyOtpScreen composable
//            // VerifyOtpScreen(
//            //     phoneNumber = phone,
//            //     onLoginSuccess = {
//            //         navController.navigate(TruckerDestination.LoadBoard.route) {
//            //             popUpTo(TruckerDestination.Login.route) { inclusive = true }
//            //         }
//            //     }
//            // )
//        }

        // 3. LoadBoard
        composable(TruckerDestination.Main.route) {
            TruckerHomeScreen(navController = navController)
//        composable(TruckerDestination.Dashboard.route) {
            // Replace with your actual LoadBoardScreen composable
            // LoadBoardScreen(
            //     onTripClick = { tripId ->
            //         navController.navigate(TruckerDestination.TripDetail.createRoute(tripId))
            //     }
            // )
        }

        // 4️⃣ Trip detail (optional, later)
        composable(
            route = TruckerDestination.TripDetail.route,
            arguments =
                listOf(
                    navArgument("tripId") { type = NavType.StringType },
                ),
        ) {
            TripDetailsScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(
            route = TruckerDestination.TruckDetail.route,
            arguments =
                listOf(
                    navArgument("truckId") { type = NavType.StringType },
                ),
        ) {
            TruckDetailsScreen(
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(
            route = TruckerDestination.OnboardTruck.route,
        ) {
            OnboardTruckScreen(
                onBackClick = { navController.popBackStack() },
                onSuccessAction = {
                    navController.popBackStack(TruckerDestination.Main.route, false)
                },
                navController = navController,
            )
        }
    }
}
