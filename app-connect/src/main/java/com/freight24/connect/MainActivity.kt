package com.freight24.connect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
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
                val isLoggedIn by sessionManager.isLoggedIn.collectAsState(initial = false)
                val lastRefreshError by sessionManager.lastRefreshError.collectAsState(initial = null)
                var isBootstrapped by remember { mutableStateOf(false) }
                var isAuthenticated by remember { mutableStateOf(false) }
                var sessionExpiredMessage by remember { mutableStateOf<String?>(null) }

                val loginRoute = if (BuildConfig.FEATURE_NEW_LOGIN) {
                    ConnectDestination.NewLogin.route
                } else {
                    ConnectDestination.Login.route
                }

                LaunchedEffect(Unit) {
                    val authenticated = sessionManager.bootstrapSession()
                    if (!authenticated) {
                        sessionExpiredMessage = sessionManager.consumeLastRefreshError()
                    }
                    isAuthenticated = authenticated
                    isBootstrapped = true
                }

                LaunchedEffect(isBootstrapped, isLoggedIn, lastRefreshError) {
                    if (isBootstrapped && !isLoggedIn && sessionExpiredMessage == null && !lastRefreshError.isNullOrBlank()) {
                        sessionExpiredMessage = lastRefreshError
                        sessionManager.consumeLastRefreshError()
                    }
                }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission(),
                ) { }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (!isBootstrapped) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val startDestination =
                            remember(isAuthenticated) {
                                if (isAuthenticated) {
                                    ConnectDestination.Home.route
                                } else {
                                    loginRoute
                                }
                            }

                        LaunchedEffect(isLoggedIn, isBootstrapped) {
                            if (isBootstrapped && !isLoggedIn) {
                                navController.navigate(loginRoute) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }

                        ConnectNavGraph(
                            navHostController = navController,
                            startDestination = startDestination,
                            sessionExpiredMessage = sessionExpiredMessage,
                            onSessionExpiredMessageShown = { sessionExpiredMessage = null },
                        )
                    }
                }
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
