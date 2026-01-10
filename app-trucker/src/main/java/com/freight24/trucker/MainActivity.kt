package com.freight24.trucker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.freight24.auth.SessionManager
import com.freight24.trucker.navigation.TruckerDestination
import com.freight24.trucker.navigation.TruckerNavGraph
import com.freight24.ui.theme.Freight24Theme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            Freight24Theme {
                val navController = rememberNavController()
                val startDestination =
                    remember {
                        if (sessionManager.isSessionValid()) {
                            TruckerDestination.Main.route
                        } else {
                            TruckerDestination.Login.route
                        }
                    }
                TruckerNavGraph(navController, startDestination)
            }
        }
    }
}
