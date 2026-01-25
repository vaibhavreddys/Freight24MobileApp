package com.freight24.connect

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.freight24.connect.navigation.ConnectDestination
import com.freight24.connect.navigation.ConnectNavGraph
import com.freight24.ui.theme.Freight24Theme
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Freight24Theme {
                val navController = rememberNavController()
                ConnectNavGraph(
                    navHostController = navController,
                    startDestination = ConnectDestination.Login.route
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        FirebaseMessaging
            .getInstance()
            .token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.w("FCM", "Token is $token")
            })
    }
}
