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
import com.freight24.trucker.feature.profile.view.AppSettingsViewModel
import com.freight24.trucker.feature.trips.view.details.TripDetailsScreen
import com.freight24.trucker.feature.trucks.view.details.TruckDetailsScreen
import com.freight24.trucker.feature.trucks.view.onboard.OnboardTruckScreen
import com.freight24.ui.components.AppSettingsScreen
import com.freight24.ui.theme.FreightAppTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TruckerNavGraph(
    navController: NavHostController,
    startDestination: String,
    sessionExpiredMessage: String? = null,
    onSessionExpiredMessageShown: () -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // 1. Login Screen
        composable(TruckerDestination.Login.route) {
            LoginScreen(
                navController = navController,
                sessionExpiredMessage = sessionExpiredMessage,
                onSessionExpiredMessageShown = onSessionExpiredMessageShown,
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
                onEditClick = { truckId ->
                    navController.navigate(TruckerDestination.EditTruck.createRoute(truckId))
                }
            )
        }

        composable(
            route = TruckerDestination.EditTruck.route,
            arguments = listOf(navArgument("truckId") { type = NavType.StringType })
        ) {
            com.freight24.trucker.feature.trucks.view.onboard.EditTruckScreen(
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

        composable(TruckerDestination.Settings.route) {
            val viewModel: AppSettingsViewModel = hiltViewModel()
            val themeOrdinal by viewModel.themeChoice.collectAsState()
            val darkMode by viewModel.darkModeOverride.collectAsState()
            val appTheme = FreightAppTheme.entries.getOrElse(themeOrdinal) { FreightAppTheme.LOGISTICS }

            AppSettingsScreen(
                currentTheme = appTheme,
                currentDarkMode = darkMode,
                onThemeChange = viewModel::setTheme,
                onDarkModeChange = viewModel::setDarkMode,
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
