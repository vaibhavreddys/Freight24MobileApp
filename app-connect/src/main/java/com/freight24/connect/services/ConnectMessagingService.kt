package com.freight24.connect.services

import android.util.Log
import com.freight24.common.manager.SharedPreferencesManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConnectMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var preferencesManager: SharedPreferencesManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        preferencesManager.firebaseToken = token
        Log.d("FCM_MESSAGE", "Token is: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCM_MESSAGE", "Message received from: ${message.from}")

        // Handle incoming data message
        message.data.isNotEmpty().let {
            Log.d("FCM_MESSAGE", "Message data payload: " + message.data)
        }

        // Handle incoming notification message
        message.notification?.let {
            Log.d("FCM_MESSAGE", "Message Notification Body: ${it.body}")
        }
    }
}
