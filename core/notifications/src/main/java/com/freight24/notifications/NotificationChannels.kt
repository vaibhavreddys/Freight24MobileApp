package com.freight24.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build

object NotificationChannels {
    const val CRITICAL = "critical_events"
    const val GENERAL = "general_updates"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val critical =
                NotificationChannel(
                    CRITICAL,
                    "F24 Alerts",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    enableVibration(true)
                    setSound(soundUri, Notification.AUDIO_ATTRIBUTES_DEFAULT)
                    setBypassDnd(true)
                }

            val general =
                NotificationChannel(
                    GENERAL,
                    "General Updates",
                    NotificationManager.IMPORTANCE_DEFAULT,
                )

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(critical)
            manager.createNotificationChannel(general)
        }
    }
}
