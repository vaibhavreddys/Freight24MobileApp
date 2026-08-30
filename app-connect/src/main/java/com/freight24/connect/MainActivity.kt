package com.freight24.connect

import android.Manifest
import android.content.Intent
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
import com.freight24.common.manager.ThemePreferenceManager
import com.freight24.connect.navigation.ConnectDestination
import com.freight24.connect.navigation.ConnectNavGraph
import com.freight24.connect.services.ConnectMessagingService
import com.freight24.ui.connect.ConnectSplashScreen
import com.freight24.ui.connect.ConnectTruckMark
import com.freight24.ui.theme.Freight24Theme
import com.freight24.ui.theme.FreightAppTheme
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var themePreferenceManager: ThemePreferenceManager

    private var pendingIndentId by mutableStateOf<String?>(null)
    private var pendingOpenInterests by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLinkIntent(intent)

        setContent {
            val themeChoiceOrdinal by themePreferenceManager.themeChoice.collectAsState(initial = ThemePreferenceManager.THEME_LOGISTICS)
            val darkModeOverride by themePreferenceManager.darkModeOverride.collectAsState(initial = ThemePreferenceManager.DARK_MODE_FOLLOW_SYSTEM)
            val appTheme = remember(themeChoiceOrdinal) {
                // Default (persisted THEME_LOGISTICS) resolves to the premium
                // CONNECT identity; an explicit user pick is honoured as-is.
                if (themeChoiceOrdinal == ThemePreferenceManager.THEME_LOGISTICS) {
                    FreightAppTheme.CONNECT
                } else {
                    FreightAppTheme.entries.getOrElse(themeChoiceOrdinal) { FreightAppTheme.CONNECT }
                }
            }

            Freight24Theme(
                appTheme = appTheme,
                darkModeOverride = darkModeOverride,
            ) {
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

                LaunchedEffect(isBootstrapped, isAuthenticated, pendingIndentId) {
                    Log.d("FCM_DEBUG", "Nav check: isBootstrapped=$isBootstrapped isAuthenticated=$isAuthenticated pendingIndentId=$pendingIndentId pendingOpenInterests=$pendingOpenInterests")
                    if (isBootstrapped && isAuthenticated && pendingIndentId != null) {
                        val indentId = pendingIndentId!!
                        val openInterests = pendingOpenInterests
                        pendingIndentId = null
                        val route = ConnectDestination.IndentDetails.createRoute(indentId, openInterests)
                        Log.d("FCM_DEBUG", "Navigating to: $route")
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                        Log.d("FCM_DEBUG", "Navigation called")
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
                        ConnectSplashScreen(logo = ConnectTruckMark)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent) {
        Log.d("FCM_DEBUG", "handleDeepLinkIntent called: intent=$intent extras=${intent.extras}")
        if (intent.hasExtra(ConnectMessagingService.EXTRA_INDENT_ID)) {
            pendingIndentId = intent.getStringExtra(ConnectMessagingService.EXTRA_INDENT_ID)
            val type = intent.getStringExtra(EXTRA_TYPE)
            pendingOpenInterests =
                intent.getBooleanExtra(ConnectMessagingService.EXTRA_OPEN_INTERESTS, false) ||
                    type == INDENT_INTEREST_TYPE
            Log.d("FCM_DEBUG", "Deep link extras found: indentId=$pendingIndentId type=$type openInterests=$pendingOpenInterests")
        } else {
            Log.d("FCM_DEBUG", "No indent_id extra in intent")
        }
    }

    companion object {
        const val EXTRA_TYPE = "type"
        const val INDENT_INTEREST_TYPE = "INDENT_INTEREST"
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

                    val token = task.result
                    Log.w("FCM", "Token is $token")
                },
            )
    }
}
