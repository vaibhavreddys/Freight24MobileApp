package com.freight24.connect.services

import android.util.Log
import com.freight24.common.manager.SharedPreferencesManager
import com.freight24.notifications.AppNotification
import com.freight24.notifications.NotificationDispatcher
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConnectMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var preferencesManager: SharedPreferencesManager

    private lateinit var notificationDispatcher: NotificationDispatcher

    override fun onCreate() {
        super.onCreate()
        notificationDispatcher = NotificationDispatcher(applicationContext)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        preferencesManager.firebaseToken = token
        Log.d("FCM_MESSAGE", "Token is: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM_MESSAGE", "Message received from: ${message.from}")

        val title = message.notification?.title ?: message.data["title"] ?: "Freight24"
        val body = message.notification?.body ?: message.data["body"] ?: return
        val type = message.data["type"]

        Log.d("FCM_MESSAGE", "Notification: type=$type title=$title body=$body")

        val appNotification = AppNotification.InterestAlert(title = title, message = body)
        notificationDispatcher.show(appNotification)
    }
}
