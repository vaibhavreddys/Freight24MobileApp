package com.freight24.notifications

import android.app.PendingIntent
import android.content.Context
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationDispatcher
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        fun show(
            notification: AppNotification,
            pendingIntent: PendingIntent? = null,
        ) {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

            NotificationChannels.create(context)

            val channelId =
                when (notification) {
                    is AppNotification.Critical -> NotificationChannels.CRITICAL
                    is AppNotification.InterestAlert -> NotificationChannels.CRITICAL
                    else -> NotificationChannels.GENERAL
                }

            val builder =
                NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_logo_freight24)
                    .setContentTitle(notification.title)
                    .setContentText(notification.message)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setVibrate(NotificationChannels.VIBRATION_PATTERN)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)

            if (pendingIntent != null) {
                builder.setContentIntent(pendingIntent)
            }

            NotificationManagerCompat.from(context)
                .notify(notification.id, builder.build())
        }
    }
