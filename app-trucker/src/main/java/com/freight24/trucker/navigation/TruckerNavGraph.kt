package com.freight24.trucker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.freight24.trucker.feature.home.TruckerHomeScreen
import com.freight24.trucker.feature.login.view.signin.NewLoginScreen
import com.freight24.trucker.feature.login.view.verify.VerifyOtpScreen
import com.freight24.trucker.feature.trips.view.details.TripDetailsScreen
import com.freight24.trucker.feature.trucks.view.details.TruckDetailsScreen
import com.freight24.trucker.feature.trucks.view.onboard.EditTruckScreen
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
        composable(TruckerDestination.NewLogin.route) {
            NewLoginScreen(
                onNavigateToOtpVerify = { phone ->
                    navController.navigate(TruckerDestination.VerifyOtp.createRoute(phone))
                }
            )
        }
//        composable(TruckerDestination.Login.route) {
//            NewLoginScreen(
//                onNavigateToOtpVerify = { phone ->
//                    navController.navigate(TruckerDestination.VerifyOtp.createRoute(phone))
//                }
//            )
//        }

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
                        popUpTo(TruckerDestination.NewLogin.route) {
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
                onEditClick = { truckId ->
                    navController.navigate(TruckerDestination.EditTruck.createRoute(truckId))
                }
            )
        }

        composable(
            route = TruckerDestination.EditTruck.route,
            arguments = listOf(navArgument("truckId") { type = NavType.StringType })
        ) {
            EditTruckScreen(
                onBackClick = { navController.popBackStack() },
                onSuccessAction = {
                    navController.popBackStack(TruckerDestination.Main.route, false)
                },
                navController = navController,
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
