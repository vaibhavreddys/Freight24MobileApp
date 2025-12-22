package com.freight24.trucker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.freight24.core.ui.theme.Freight24Theme
import com.freight24.trucker.navigation.TruckerDestination
import com.freight24.trucker.navigation.TruckerNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Freight24Theme {
                val navController = rememberNavController()
//                val startDestination = remember {
//                    if (isSessionValid()) {
//                        TruckerDestinations.TruckerDestination.Dashboard.route
//                    } else {
//                        TruckerDestinations.TruckerDestination.Login.route
//                    }
//                }
                TruckerNavGraph(navController, TruckerDestination.Login.route)
            }
        }
    }
}
