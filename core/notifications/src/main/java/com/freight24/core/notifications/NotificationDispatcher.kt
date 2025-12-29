package com.freight24.core.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationDispatcher @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun show(notification: AppNotification) {
        val channelId = when (notification) {
            is AppNotification.Critical -> NotificationChannels.CRITICAL
            else -> NotificationChannels.GENERAL
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_logo_freight24)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        NotificationManagerCompat.from(context)
            .notify(notification.id, builder.build())
    }
}
