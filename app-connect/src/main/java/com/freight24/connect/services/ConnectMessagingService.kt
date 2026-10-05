package com.freight24.connect.services

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.freight24.common.manager.SharedPreferencesManager
import com.freight24.connect.MainActivity
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
        val indentId = message.data["indent_id"]

        Log.d("FCM_MESSAGE", "Notification: type=$type indentId=$indentId title=$title body=$body")

        val pendingIntent =
            if (type == "INDENT_INTEREST" && indentId != null) {
                buildIndentDeepLinkPendingIntent(indentId)
            } else {
                null
            }

        Log.d("FCM_DEBUG", "Building notification: type=$type indentId=$indentId pendingIntent=${pendingIntent != null}")

        val appNotification =
            AppNotification.InterestAlert(
                title = title,
                message = body,
                indentId = indentId,
            )
        notificationDispatcher.show(appNotification, pendingIntent)
    }

    private fun buildIndentDeepLinkPendingIntent(indentId: String): PendingIntent {
        val intent =
            Intent(this, MainActivity::class.java).apply {
                putExtra(EXTRA_INDENT_ID, indentId)
                putExtra(EXTRA_OPEN_INTERESTS, true)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        return PendingIntent.getActivity(
            this,
            indentId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val EXTRA_INDENT_ID = "indent_id"
        const val EXTRA_OPEN_INTERESTS = "open_interests"
    }
}
