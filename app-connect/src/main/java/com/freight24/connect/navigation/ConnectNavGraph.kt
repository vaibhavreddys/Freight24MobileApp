package com.freight24.connect.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.freight24.connect.feature.home.HomeScreen
import com.freight24.connect.feature.login.view.signin.LoginScreen
import com.freight24.connect.feature.login.view.verify.VerifyOtpScreen

@Composable
fun ConnectNavGraph(
    navHostController: NavHostController,
    startDestination: String = ConnectDestination.Login.route
) {
    NavHost(
        navController = navHostController,
        startDestination = startDestination,
    ) {
        composable(ConnectDestination.Login.route) {
            LoginScreen(onVerify = { phoneNumber ->
                navHostController.navigate(
                    ConnectDestination.VerifyOtp.createRoute(phoneNumber)
                )
            })
        }

        composable(
            route = ConnectDestination.VerifyOtp.route,
            arguments = ConnectDestination.VerifyOtp.arguments
        ) { backStackEntry ->

            val phoneNumber = backStackEntry
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
                }
            )
        }

        composable(ConnectDestination.Home.route) {
            HomeScreen()
        }
    }
}
