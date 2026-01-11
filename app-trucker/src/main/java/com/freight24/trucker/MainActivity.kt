package com.freight24.trucker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.freight24.core.auth.SessionManager
import com.freight24.core.ui.theme.Freight24Theme
import com.freight24.trucker.navigation.TruckerDestination
import com.freight24.trucker.navigation.TruckerNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var tokenManager: com.freight24.core.network.auth.TokenManager

    @Inject
    lateinit var networkMonitor: com.freight24.core.common.util.NetworkMonitor

    @Inject
    lateinit var globalMessageManager: com.freight24.core.common.util.GlobalMessageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
            val isOnline by networkMonitor.isOnline.collectAsState(initial = true)

            // Auth Event Observer
            LaunchedEffect(Unit) {
                tokenManager.authEvents.collect { event ->
                    if (event == com.freight24.core.network.auth.AuthEvent.LOGOUT) {
                        navController.navigate(TruckerDestination.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }

            // Global Message Observer
            LaunchedEffect(Unit) {
                globalMessageManager.messages.collect { message ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = androidx.compose.material3.SnackbarDuration.Short
                    )
                }
            }

            // Connectivity Observer
            LaunchedEffect(isOnline) {
                if (!isOnline) {
                    snackbarHostState.showSnackbar(
                        message = "No Internet Connection",
                        duration = androidx.compose.material3.SnackbarDuration.Indefinite
                    )
                } else {
                    if (snackbarHostState.currentSnackbarData?.visuals?.message == "No Internet Connection") {
                        snackbarHostState.currentSnackbarData?.dismiss()
                    }
                }
            }

            Freight24Theme {
                val startDestination = remember {
                    if (sessionManager.isSessionValid()) {
                        TruckerDestination.Main.route
                    } else {
                        TruckerDestination.Login.route
                    }
                }
                TruckerNavGraph(
                    navController = navController,
                    startDestination = startDestination,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}
