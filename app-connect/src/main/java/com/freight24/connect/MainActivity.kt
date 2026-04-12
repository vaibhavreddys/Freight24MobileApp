package com.freight24.connect

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.freight24.connect.BuildConfig
import com.freight24.auth.SessionManager
import com.freight24.connect.navigation.ConnectDestination
import com.freight24.connect.navigation.ConnectNavGraph
import com.freight24.ui.theme.Freight24Theme
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Freight24Theme {
                val navController = rememberNavController()
                val isLoggedIn by sessionManager.isLoggedIn.collectAsState(initial = sessionManager.isSessionValid())

                val loginRoute = if (BuildConfig.FEATURE_NEW_LOGIN) {
                    ConnectDestination.NewLogin.route
                } else {
                    ConnectDestination.Login.route
                }
                val startDestination =
                    remember {
                        if (sessionManager.isSessionValid()) {
                            ConnectDestination.Home.route
                        } else {
                            loginRoute
                        }
                    }
                LaunchedEffect(isLoggedIn) {
                    if (!isLoggedIn) {
                        // Clear backstack and redirect to login on 401 or manual logout
                        navController.navigate(loginRoute) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
                ConnectNavGraph(
                    navHostController = navController,
                    startDestination = startDestination,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        FirebaseMessaging
            .getInstance()
            .token
            .addOnCompleteListener(
                OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new FCM registration token
                    val token = task.result
                    Log.w("FCM", "Token is $token")
                },
            )
    }
}
