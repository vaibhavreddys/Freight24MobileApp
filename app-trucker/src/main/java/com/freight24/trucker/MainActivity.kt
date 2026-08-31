package com.freight24.trucker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.core.view.WindowCompat
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.freight24.auth.SessionManager
import com.freight24.common.manager.ThemePreferenceManager
import com.freight24.trucker.navigation.TruckerDestination
import com.freight24.trucker.navigation.TruckerNavGraph
import com.freight24.ui.theme.Freight24Theme
import com.freight24.ui.theme.FreightAppTheme
import com.freight24.ui.trucker.TruckerSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var themePreferenceManager: ThemePreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val themeChoiceOrdinal by themePreferenceManager.themeChoice.collectAsState(initial = ThemePreferenceManager.THEME_LOGISTICS)
            val darkModeOverride by themePreferenceManager.darkModeOverride.collectAsState(initial = ThemePreferenceManager.DARK_MODE_FOLLOW_SYSTEM)
            val appTheme = remember(themeChoiceOrdinal) {
                FreightAppTheme.entries.getOrElse(themeChoiceOrdinal) { FreightAppTheme.INDIGO }
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

                LaunchedEffect(Unit) {
                    val bootstrapStarted = android.os.SystemClock.uptimeMillis()
                    val authenticated = sessionManager.bootstrapSession()
                    if (!authenticated) {
                        sessionExpiredMessage = sessionManager.consumeLastRefreshError()
                    }
                    isAuthenticated = authenticated
                    // Hold the splash until the one-shot choreography has
                    // landed (F24 in → unfurl → road ≈ 1.3s), even when
                    // bootstrap finishes instantly on a cached session.
                    val remaining = SPLASH_MIN_DISPLAY_MS -
                        (android.os.SystemClock.uptimeMillis() - bootstrapStarted)
                    if (remaining > 0) delay(remaining)
                    isBootstrapped = true
                }

                LaunchedEffect(isBootstrapped, isLoggedIn, lastRefreshError) {
                    if (isBootstrapped && !isLoggedIn && sessionExpiredMessage == null && !lastRefreshError.isNullOrBlank()) {
                        sessionExpiredMessage = lastRefreshError
                        sessionManager.consumeLastRefreshError()
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Crossfade(
                        targetState = isBootstrapped,
                        animationSpec = tween(durationMillis = 350),
                        label = "splashExit",
                        modifier = Modifier.fillMaxSize(),
                    ) { bootstrapped ->
                        if (!bootstrapped) {
                            TruckerSplashScreen()
                        } else {
                            val startDestination =
                                remember(isAuthenticated) {
                                    if (isAuthenticated) {
                                        TruckerDestination.Main.route
                                    } else {
                                        TruckerDestination.Login.route
                                    }
                                }

                            LaunchedEffect(isLoggedIn, isBootstrapped) {
                                if (isBootstrapped && !isLoggedIn) {
                                    navController.navigate(TruckerDestination.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }

                            TruckerNavGraph(
                                navController = navController,
                                startDestination = startDestination,
                                sessionExpiredMessage = sessionExpiredMessage,
                                onSessionExpiredMessageShown = { sessionExpiredMessage = null },
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Splash stays up long enough for the one-shot choreography — F24 in,
         * unfurl to FREIGHT 24, road draw (~1.3s) — to land before the
         * crossfade.
         */
        private const val SPLASH_MIN_DISPLAY_MS = 1500L
    }
}
